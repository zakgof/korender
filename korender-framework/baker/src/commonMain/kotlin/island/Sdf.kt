package island

import com.zakgof.korender.math.Vec2
import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.sign
import kotlin.math.sqrt

class Sdf(private val reso: Int) {

    private class Spline(
        val x: PolynomialSplineFunction,
        val y: PolynomialSplineFunction,
        val maxT: Float
    ) {
        val dx: UnivariateFunction = x.derivative()
        val dy: UnivariateFunction = y.derivative()
    }

    private class Pt(
        val t: Float,
        val x: Float,
        val y: Float,
        val dx: Float,
        val dy: Float
    )

    private data class Cell(
        val x: Int,
        val y: Int
    )

    private class SdfPt(
        val t: Float,
        val d2: Float,
        val sign: Float
    )

    private val splines = mutableListOf<Spline>()
    private val candidates = mutableMapOf<Cell, SdfPt>()

    fun spline(waypoints: List<Vec2>) {
        val doubx = waypoints.map { (it.x.toDouble()) }.toDoubleArray()
        val douby = waypoints.map { (it.y.toDouble()) }.toDoubleArray()
        val lengths = waypoints.indices.map {
            if (it == 0) 0.0 else sqrt((doubx[it] - doubx[it - 1]) * (doubx[it] - doubx[it - 1]) + (douby[it] - douby[it - 1]) * (douby[it] - douby[it - 1]))
        }
        val doubt = lengths.fold(mutableListOf<Double>()) { acc, v ->
            val currentSum = (acc.lastOrNull() ?: 0.0) + v
            acc.add(currentSum)
            acc
        }.toDoubleArray()

        val splineInterpolator = SplineInterpolator()
        val xspline = splineInterpolator.interpolate(doubt, doubx)
        val yspline = splineInterpolator.interpolate(doubt, douby)

        splines += Spline(xspline, yspline, doubt.last().toFloat())
    }

    private fun curve(t: Float, spline: Spline) =
        Pt(
            t,
            spline.x.value(t.toDouble()).toFloat(),
            spline.y.value(t.toDouble()).toFloat(),
            spline.dx.value(t.toDouble()).toFloat(),
            spline.dy.value(t.toDouble()).toFloat()
        )

    fun save(file: String) {
        val steps = 2000
        val distanceScale = 0.01f
        val cellRange = ceil(distanceScale * reso).toInt() + 1

        splines.forEach { spline ->
            for (step in 0..steps) {
                val t = spline.maxT * 0.999f * step / steps
                val pt = curve(t, spline)
                pt.cell().neighbors(cellRange).forEach { cell ->
                    val oldCandidate = candidates[cell]
                    val candidate = sdf(pt, cell, spline)
                    if (oldCandidate == null || oldCandidate.d2 > candidate.d2) {
                        candidates[cell] = candidate
                    }
                }
                println(step * 100 / steps)
            }
        }

        val output = File(file)
        val img = BufferedImage(reso, reso, BufferedImage.TYPE_INT_ARGB)
        val raster = img.raster
        val pixel = FloatArray(4)
        for (xx in 0 until reso) {
            for (zz in 0 until reso) {
                val c = candidates[Cell(xx, zz)]
                pixel[2] = 0f
                pixel[3] = 255f
                if (c == null) {
                    pixel[0] = 255f
                    pixel[1] = 0f
                    pixel[2] = 0f
                } else {
                    // 0..distanceScale sign+ -> 0.5..1.0
                    // 0..distanceScale sign- -> 0.5..1.0
                    pixel[0] = (255f * (0.5f + 0.5f * c.sign * (sqrt(c.d2) / distanceScale))).coerceIn(0f, 255f)
                    pixel[1] = 255f * c.t
                    pixel[2] = if (pixel[0] < 10f || pixel[0] > 244f) 0f else 255f
                }
                raster.setPixel(xx, zz, pixel)
            }
        }
        ImageIO.write(img, "png", output)
    }

    private fun Pt.cell(): Cell = Cell((x * reso).toInt(), (y * reso).toInt())

    private fun neighbors1d(a: Int, cellRange: Int) = (a - cellRange..a + cellRange).filter { it >= 0 && it < reso }

    private fun Cell.neighbors(cellRange: Int) =
        neighbors1d(x, cellRange)
            .flatMap { xx ->
                neighbors1d(y, cellRange).map { yy ->
                    Cell(xx, yy)
                }
            }

    private fun sdf(pt: Pt, cell: Cell, spline: Spline): SdfPt {
        val cellX = (cell.x + 0.5f) / reso
        val cellY = (cell.y + 0.5f) / reso
        val offsetX = pt.x - cellX
        val offsetY = pt.y - cellY
        val sign = sign(offsetX * pt.dy - offsetY * pt.dx)
        return SdfPt(pt.t / spline.maxT, offsetX * offsetX + offsetY * offsetY, sign)
    }
}

