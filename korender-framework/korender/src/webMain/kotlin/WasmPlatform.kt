package com.zakgof.korender

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.zakgof.korender.scope.KorenderScope
import com.zakgof.korender.impl.buffer.NativeByteBuffer
import com.zakgof.korender.impl.engine.Engine
import com.zakgof.korender.impl.font.FontDef
import com.zakgof.korender.impl.gl.GL
import com.zakgof.korender.impl.image.InternalImage
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.WebGLRenderingContext.Companion.RENDERER
import org.khronos.webgl.WebGLRenderingContext.Companion.SHADING_LANGUAGE_VERSION
import org.khronos.webgl.WebGLRenderingContext.Companion.VENDOR
import org.khronos.webgl.WebGLRenderingContext.Companion.VERSION
import org.khronos.webgl.toInt8Array
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private class WebKeyMapping(val key: String, val composeKey: Key)

private val WEB_KEY_MAPPING: Map<String, WebKeyMapping> = mapOf(
    "KeyA" to WebKeyMapping("A", Key.A),
    "KeyB" to WebKeyMapping("B", Key.B),
    "KeyC" to WebKeyMapping("C", Key.C),
    "KeyD" to WebKeyMapping("D", Key.D),
    "KeyE" to WebKeyMapping("E", Key.E),
    "KeyF" to WebKeyMapping("F", Key.F),
    "KeyG" to WebKeyMapping("G", Key.G),
    "KeyH" to WebKeyMapping("H", Key.H),
    "KeyI" to WebKeyMapping("I", Key.I),
    "KeyJ" to WebKeyMapping("J", Key.J),
    "KeyK" to WebKeyMapping("K", Key.K),
    "KeyL" to WebKeyMapping("L", Key.L),
    "KeyM" to WebKeyMapping("M", Key.M),
    "KeyN" to WebKeyMapping("N", Key.N),
    "KeyO" to WebKeyMapping("O", Key.O),
    "KeyP" to WebKeyMapping("P", Key.P),
    "KeyQ" to WebKeyMapping("Q", Key.Q),
    "KeyR" to WebKeyMapping("R", Key.R),
    "KeyS" to WebKeyMapping("S", Key.S),
    "KeyT" to WebKeyMapping("T", Key.T),
    "KeyU" to WebKeyMapping("U", Key.U),
    "KeyV" to WebKeyMapping("V", Key.V),
    "KeyW" to WebKeyMapping("W", Key.W),
    "KeyX" to WebKeyMapping("X", Key.X),
    "KeyY" to WebKeyMapping("Y", Key.Y),
    "KeyZ" to WebKeyMapping("Z", Key.Z),

    "Digit0" to WebKeyMapping("0", Key.Zero),
    "Digit1" to WebKeyMapping("1", Key.One),
    "Digit2" to WebKeyMapping("2", Key.Two),
    "Digit3" to WebKeyMapping("3", Key.Three),
    "Digit4" to WebKeyMapping("4", Key.Four),
    "Digit5" to WebKeyMapping("5", Key.Five),
    "Digit6" to WebKeyMapping("6", Key.Six),
    "Digit7" to WebKeyMapping("7", Key.Seven),
    "Digit8" to WebKeyMapping("8", Key.Eight),
    "Digit9" to WebKeyMapping("9", Key.Nine),

    "F1" to WebKeyMapping("F1", Key.F1),
    "F2" to WebKeyMapping("F2", Key.F2),
    "F3" to WebKeyMapping("F3", Key.F3),
    "F4" to WebKeyMapping("F4", Key.F4),
    "F5" to WebKeyMapping("F5", Key.F5),
    "F6" to WebKeyMapping("F6", Key.F6),
    "F7" to WebKeyMapping("F7", Key.F7),
    "F8" to WebKeyMapping("F8", Key.F8),
    "F9" to WebKeyMapping("F9", Key.F9),
    "F10" to WebKeyMapping("F10", Key.F10),
    "F11" to WebKeyMapping("F11", Key.F11),
    "F12" to WebKeyMapping("F12", Key.F12),

    "Enter" to WebKeyMapping("ENTER", Key.Enter),
    "Escape" to WebKeyMapping("ESCAPE", Key.Escape),
    "Backspace" to WebKeyMapping("BACKSPACE", Key.Backspace),
    "Tab" to WebKeyMapping("TAB", Key.Tab),
    "Space" to WebKeyMapping("SPACE", Key.Spacebar),
    "Insert" to WebKeyMapping("INSERT", Key.Insert),
    "Delete" to WebKeyMapping("DELETE", Key.Delete),
    "Home" to WebKeyMapping("HOME", Key.MoveHome),
    "PageUp" to WebKeyMapping("PAGEUP", Key.PageUp),
    "PageDown" to WebKeyMapping("PAGEDOWN", Key.PageDown),

    "ArrowLeft" to WebKeyMapping("LEFT", Key.DirectionLeft),
    "ArrowRight" to WebKeyMapping("RIGHT", Key.DirectionRight),
    "ArrowUp" to WebKeyMapping("UP", Key.DirectionUp),
    "ArrowDown" to WebKeyMapping("DOWN", Key.DirectionDown),

    "ShiftLeft" to WebKeyMapping("SHIFT", Key.ShiftLeft),
    "ShiftRight" to WebKeyMapping("SHIFT", Key.ShiftRight),
    "ControlLeft" to WebKeyMapping("CONTROL", Key.CtrlLeft),
    "ControlRight" to WebKeyMapping("CONTROL", Key.CtrlRight),
    "AltLeft" to WebKeyMapping("ALT", Key.AltLeft),
    "AltRight" to WebKeyMapping("ALT", Key.AltRight),
    "MetaLeft" to WebKeyMapping("META", Key.MetaLeft),
    "MetaRight" to WebKeyMapping("META", Key.MetaRight),
    "CapsLock" to WebKeyMapping("CAPSLOCK", Key.CapsLock),
    "NumLock" to WebKeyMapping("NUMLOCK", Key.NumLock),
    "ScrollLock" to WebKeyMapping("SCROLLLOCK", Key.ScrollLock),

    "Numpad0" to WebKeyMapping("NUMPAD0", Key.NumPad0),
    "Numpad1" to WebKeyMapping("NUMPAD1", Key.NumPad1),
    "Numpad2" to WebKeyMapping("NUMPAD2", Key.NumPad2),
    "Numpad3" to WebKeyMapping("NUMPAD3", Key.NumPad3),
    "Numpad4" to WebKeyMapping("NUMPAD4", Key.NumPad4),
    "Numpad5" to WebKeyMapping("NUMPAD5", Key.NumPad5),
    "Numpad6" to WebKeyMapping("NUMPAD6", Key.NumPad6),
    "Numpad7" to WebKeyMapping("NUMPAD7", Key.NumPad7),
    "Numpad8" to WebKeyMapping("NUMPAD8", Key.NumPad8),
    "Numpad9" to WebKeyMapping("NUMPAD9", Key.NumPad9),
    "NumpadAdd" to WebKeyMapping("NUMPADADD", Key.NumPadAdd),
    "NumpadSubtract" to WebKeyMapping("NUMPADSUBTRACT", Key.NumPadSubtract),
    "NumpadMultiply" to WebKeyMapping("NUMPADMULTIPLY", Key.NumPadMultiply),
    "NumpadDivide" to WebKeyMapping("NUMPADDIVIDE", Key.NumPadDivide),

    "Comma" to WebKeyMapping(",", Key.Comma),
    "Period" to WebKeyMapping(".", Key.Period),
    "Slash" to WebKeyMapping("/", Key.Slash),
    "Backslash" to WebKeyMapping("\\", Key.Backslash),
    "Semicolon" to WebKeyMapping(";", Key.Semicolon),
    "Equal" to WebKeyMapping("=", Key.Equals),
    "Minus" to WebKeyMapping("-", Key.Minus),
    "BracketLeft" to WebKeyMapping("[", Key.LeftBracket),
    "BracketRight" to WebKeyMapping("]", Key.RightBracket),
    "Quote" to WebKeyMapping("'", Key.Apostrophe),
    "Backquote" to WebKeyMapping("BACKTICK", Key.Grave),
)

@OptIn(ExperimentalWasmJsInterop::class)
internal actual object Platform {

    actual val target = KorenderScope.TargetPlatform.Web

    @OptIn(DelicateCoroutinesApi::class)
    internal actual fun loadFont(bytes: ByteArray): Deferred<FontDef> {
        val ffLoader = jsLoadFont(bytes.toInt8Array())
        return GlobalScope.async {
            val fontFace = ffLoader.load().await<FontFace>()
            jsAddFont(fontFace)
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.height = 16 * 256
            canvas.width = 16 * 256
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            ctx.font = "256px KorenderFont"
            val texts = (0 until 128).map { "" + it.toChar() }
            val origMetrics = texts.map { ctx.measureText(it) }
            val actualBbHeight = origMetrics.maxOfOrNull { it.actualBoundingBoxDescent + it.actualBoundingBoxAscent }!!
            val fittingHeight = (256 * 256 / actualBbHeight).toInt() + 1
            println("Effective height: $fittingHeight")
            ctx.font = "${fittingHeight}px KorenderFont"
            ctx.fillStyle = "white".toJsString()

            val metrics = texts.map { ctx.measureText(it) }
            val maxDescent = origMetrics.maxOfOrNull { it.actualBoundingBoxDescent }!!

            ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
            val widths = metrics.map { it.width.toFloat() / 256.0f }.toFloatArray()

            texts.indices.forEach {
                ctx.fillText(
                    texts[it],
                    (it % 16) * 256.0,
                    (it / 16) * 256.0 + 256.0 - maxDescent
                )
            }
            val imageData = ctx.getImageData(
                0.0,
                0.0,
                ctx.canvas.width.toDouble(),
                ctx.canvas.height.toDouble()
            )
            val uint8ClampedArray: Uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            val image = InternalImage(
                ctx.canvas.width,
                ctx.canvas.height,
                NativeByteBuffer(uint8Array),
                PixelFormat.RGBA
            )
            FontDef(image, widths)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    internal actual fun loadImage(bytes: ByteArray, type: String): Deferred<InternalImage> {
        val result = CompletableDeferred<InternalImage>()
        val base64Data = Base64.encode(bytes)
        val image = document.createElement("img") as HTMLImageElement
        image.src = "data:image/$type;base64,$base64Data"
        image.onerror = { a, b, c, d, e ->
            result.completeExceptionally(KorenderException("$a $b $c $d $e"))
            null
        }
        image.onload = {
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val context = canvas.getContext("2d") as CanvasRenderingContext2D
            canvas.width = image.width
            canvas.height = image.height
            context.drawImage(image, 0.0, 0.0)
            val imageData = context.getImageData(
                0.0,
                0.0,
                canvas.width.toDouble(),
                canvas.height.toDouble()
            )
            val uint8ClampedArray: Uint8ClampedArray = imageData.data
            val uint8Array = Uint8Array(uint8ClampedArray.buffer, uint8ClampedArray.byteOffset, uint8ClampedArray.length)
            result.complete(InternalImage(imageData.width, imageData.height, NativeByteBuffer(uint8Array), PixelFormat.RGBA))
        }
        return result
    }

    actual fun createImage(width: Int, height: Int, format: PixelFormat): InternalImage {
        val pixelBytes = when(format) {
            PixelFormat.RGB -> 3
            PixelFormat.RGBA -> 4
            PixelFormat.Gray -> 1
            PixelFormat.Gray16 -> 2
        }
        val buffer = NativeByteBuffer(width * height * pixelBytes)
        return InternalImage(width, height, buffer, format)
    }

    actual fun nanoTime(): Long =
        (performanceNow() * 1_000_000).toLong()

}

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun Korender(
    resourceLoader: ResourceLoader,
    vSync: Boolean,
    block: KorenderScope.() -> Unit
) {
    var engine: Engine? by remember { mutableStateOf(null) }
    val canvas by remember { mutableStateOf(document.createElement("canvas") as HTMLCanvasElement) }

    Column(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val crds = coordinates.boundsInWindow()

            canvas.width = (crds.width / window.devicePixelRatio).toInt()
            canvas.height = (crds.height / window.devicePixelRatio).toInt()
            canvas.style.apply {
                position = "absolute"
                left = "${(crds.left / window.devicePixelRatio).toInt()}px"
                top = "${(crds.top / window.devicePixelRatio)}px"
                background = "black"
            }
            engine?.resize(
                (crds.width / window.devicePixelRatio).toInt(),
                (crds.height / window.devicePixelRatio).toInt()
            )
        }.fillMaxSize() // TODO
    )
    {
    }

    DisposableEffect(Unit) {
        canvas.width = 0
        canvas.height = 0
        canvas.style.apply {
            position = "absolute"
            left = "0px"
            top = "0px"
            // background = "black"
        }
        document.documentElement!!.appendChild(canvas)

        val gl2 = canvas.getContext("webgl2")
        if (gl2 == null) {
            println("WebGL2 is not supported in this browser")
        }

        val gl = gl2 as WebGL2RenderingContext

        println("Renderer: " + gl.getParameter(RENDERER));
        println("Vendor: " + gl.getParameter(VENDOR));
        println("Version: " + gl.getParameter(VERSION));
        println("GLSL version: " + gl.getParameter(SHADING_LANGUAGE_VERSION));
        println("Supported extensions")

        val exts = gl.getSupportedExtensions()!!
        (0 until exts.length).map { exts[it] }.forEach {
            println(" - $it")
        }

        GL.gl = gl

        fun animate() {
            window.requestAnimationFrame {
                try {
                    if (canvas.isConnected) {
                        GL.gl = gl
                        engine!!.frame()
                        animate()
                    } else {
                        println("Canvas disconnected")
                    }
                } catch (e: Throwable) {
                    println(e)
                    e.printStackTrace()
                }
            }
        }

        engine = Engine(800, 600, resourceLoader, block)

        animate()

        fun Short.toButton() : TouchEvent.Button = when (this) {
            0.toShort() -> TouchEvent.Button.LEFT
            2.toShort() -> TouchEvent.Button.RIGHT
            else -> TouchEvent.Button.NONE
        }

        fun keyboardModifiers(event: MouseEvent) =
            KeyboardModifiers(event.shiftKey, event.ctrlKey, event.altKey, event.metaKey)

        fun keyboardModifiers(event: org.w3c.dom.events.KeyboardEvent) =
            KeyboardModifiers(event.shiftKey, event.ctrlKey, event.altKey, event.metaKey)

        canvas.addEventListener("webglcontextlost") {
            it.preventDefault()
            println("WebGL context lost !")
        }

        fun sendMouseTouch(type: TouchEvent.Type, event: Event) {
            val me = event as MouseEvent
            val x = me.pageX - canvas.offsetLeft
            val y = me.pageY - canvas.offsetTop
            GlobalScope.launch {
                engine?.pushTouch(TouchEvent(type, me.button.toButton(), x.toFloat(), y.toFloat(), keyboardModifiers(me)))
            }
        }

        fun sendTouchTouch(type: TouchEvent.Type, event: Event) {
            val te = event as org.w3c.dom.TouchEvent
            te.touches[0]?.let { touch ->
                val x = touch.pageX - canvas.offsetLeft
                val y = touch.pageY - canvas.offsetTop
                GlobalScope.launch {
                    engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat(), KeyboardModifiers()))
                }
            }
            // TODO improve this POC
            if (type == TouchEvent.Type.UP && te.touches[0] == null) {
                te.changedTouches[0]?.let { touch ->
                    val x = touch.pageX - canvas.offsetLeft
                    val y = touch.pageY - canvas.offsetTop
                    GlobalScope.launch {
                        engine?.pushTouch(TouchEvent(type, TouchEvent.Button.LEFT, x.toFloat(), y.toFloat(), KeyboardModifiers()))
                    }
                }
            }
        }

        fun sendKey(type: KeyEvent.Type, event: Event) {
            val ke = event as org.w3c.dom.events.KeyboardEvent
            val mapping = WEB_KEY_MAPPING[ke.code] ?: WebKeyMapping(ke.key, Key.Unknown)
            GlobalScope.launch {
                engine?.pushKey(KeyEvent(type, mapping.key, mapping.composeKey, keyboardModifiers(ke)))
            }
        }

        canvas.addEventListener("mouseup") {
            sendMouseTouch(TouchEvent.Type.UP, it)
        }
        canvas.addEventListener("mousedown") {
            sendMouseTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.addEventListener("mousemove") {
            sendMouseTouch(TouchEvent.Type.MOVE, it)
        }

        canvas.addEventListener("touchstart") {
            sendTouchTouch(TouchEvent.Type.DOWN, it)
        }
        canvas.addEventListener("touchend") {
            sendTouchTouch(TouchEvent.Type.UP, it)
        }
        canvas.addEventListener("touchmove") {
            sendTouchTouch(TouchEvent.Type.MOVE, it)
        }
        // TODO cleanup
        document.addEventListener("keydown") {
            sendKey(KeyEvent.Type.DOWN, it)
        }
        document.addEventListener("keyup") {
            sendKey(KeyEvent.Type.UP, it)
        }

        onDispose {
            println("Destroying canvas")
            val ext = gl.getExtension("WEBGL_lose_context")
            if (ext != null) {
                loseContext(ext)
            }
            canvas.remove()
        }
    }

}

