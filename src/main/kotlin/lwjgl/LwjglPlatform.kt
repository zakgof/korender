package com.zakgof.korender.lwjgl

import com.zakgof.korender.Platform
import gl.*
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil

class LwjglPlatform : Platform {

    private var window: Long = 0
    private var keyCallback: GLFWKeyCallback? = null

    init {
        VGL11.gl = Lwjgl11()
        VGL12.gl = Lwjgl12()
        VGL13.gl = Lwjgl13()
        VGL14.gl = Lwjgl14()
        VGL15.gl = Lwjgl15()
        VGL20.gl = Lwjgl20()
        VGL30.gl = Lwjgl30()
    }

    override fun run(width: Int, height: Int, init: () -> Unit, frameCallback: () -> Unit) {
        val errorCallback = GLFW.glfwSetErrorCallback(GLFWErrorCallback.createThrow())
        try {
            createWindow(width, height)
            GL.createCapabilities()
            init.invoke()
            loop(frameCallback)
            GLFW.glfwDestroyWindow(window);
        } finally {
            GLFW.glfwTerminate()
            errorCallback?.close()
            keyCallback?.close()
        }
    }

    fun createWindow(width: Int, height: Int) {

        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        // Configure our window
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        // Create the window
        window = GLFW.glfwCreateWindow(
            width, height,
            "Hello World!",
            MemoryUtil.NULL,
            MemoryUtil.NULL
        )
        if (window == MemoryUtil.NULL) {
            throw IllegalStateException("Failed to create the GLFW window")
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        keyCallback = GLFW.glfwSetKeyCallback(window, object : GLFWKeyCallback() {
            override fun invoke(
                window: Long,
                key: Int,
                scancode: Int,
                action: Int,
                mods: Int
            ) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                    GLFW.glfwSetWindowShouldClose(window, true)
                }
            }
        })

        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!
        GLFW.glfwSetWindowPos(
            window,
            (vidmode.width() - width) / 2,
            (vidmode.height() - height) / 2
        );
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(window)
    }

    private fun loop(frameCallback: () -> Unit) {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            frameCallback.invoke()
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

}
