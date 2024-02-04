package com.zakgof.korender.noise

/**
 * K.jpg's OpenSimplex 2, smooth variant ("SuperSimplex")
 *
 * More language ports, as well as legacy 2014 OpenSimplex, can be found here:
 * https://github.com/KdotJPG/OpenSimplex2
 */
object OpenSimplex2S {
    private const val PRIME_X = 0x5205402B9270C86FL
    private const val PRIME_Y = 0x598CD327003817B5L
    private const val PRIME_Z = 0x5BCC226E9FA0BACBL
    private const val PRIME_W = 0x56CC5227E58F554BL
    private const val HASH_MULTIPLIER = 0x53A3F72DEEC546F5L
    private const val SEED_FLIP_3D = -0x52D547B2E96ED629L

    private const val ROOT2OVER2 = 0.7071067811865476
    private const val SKEW_2D = 0.366025403784439
    private const val UNSKEW_2D = -0.21132486540518713

    private const val ROOT3OVER3 = 0.577350269189626
    private const val FALLBACK_ROTATE3 = 2.0 / 3.0
    private const val ROTATE3_ORTHOGONALIZER = UNSKEW_2D

    private const val SKEW_4D = 0.309016994374947f
    private const val UNSKEW_4D = -0.138196601125011f

    private const val N_GRADS_2D_EXPONENT = 7
    private const val N_GRADS_3D_EXPONENT = 8
    private const val N_GRADS_4D_EXPONENT = 9
    private const val N_GRADS_2D = 1 shl N_GRADS_2D_EXPONENT
    private const val N_GRADS_3D = 1 shl N_GRADS_3D_EXPONENT
    private const val N_GRADS_4D = 1 shl N_GRADS_4D_EXPONENT

    private const val NORMALIZER_2D = 0.05481866495625118
    private const val NORMALIZER_3D = 0.2781926117527186
    private const val NORMALIZER_4D = 0.11127401889945551

    private const val RSQUARED_2D = 2.0f / 3.0f
    private const val RSQUARED_3D = 3.0f / 4.0f
    private const val RSQUARED_4D = 4.0f / 5.0f

    /*
     * Noise Evaluators
     */
    /**
     * 2D OpenSimplex2S/SuperSimplex noise, standard lattice orientation.
     */
    fun noise2(seed: Long, x: Double, y: Double): Float {
        // Get points for A2* lattice

        val s = SKEW_2D * (x + y)
        val xs = x + s
        val ys = y + s

        return noise2_UnskewedBase(seed, xs, ys)
    }

    /**
     * 2D OpenSimplex2S/SuperSimplex noise, with Y pointing down the main diagonal.
     * Might be better for a 2D sandbox style game, where Y is vertical.
     * Probably slightly less optimal for heightmaps or continent maps,
     * unless your map is centered around an equator. It's a slight
     * difference, but the option is here to make it easy.
     */
    fun noise2_ImproveX(seed: Long, x: Double, y: Double): Float {
        // Skew transform and rotation baked into one.

        val xx = x * ROOT2OVER2
        val yy = y * (ROOT2OVER2 * (1 + 2 * SKEW_2D))

        return noise2_UnskewedBase(seed, yy + xx, yy - xx)
    }

    /**
     * 2D  OpenSimplex2S/SuperSimplex noise base.
     */
    private fun noise2_UnskewedBase(seed: Long, xs: Double, ys: Double): Float {
        // Get base points and offsets.

        val xsb = fastFloor(xs)
        val ysb = fastFloor(ys)
        val xi = (xs - xsb).toFloat()
        val yi = (ys - ysb).toFloat()

        // Prime pre-multiplication for hash.
        val xsbp = xsb * PRIME_X
        val ysbp = ysb * PRIME_Y

        // Unskew.
        val t = (xi + yi) * UNSKEW_2D.toFloat()
        val dx0 = xi + t
        val dy0 = yi + t

        // First vertex.
        val a0 = RSQUARED_2D - dx0 * dx0 - dy0 * dy0
        var value = (a0 * a0) * (a0 * a0) * grad(seed, xsbp, ysbp, dx0, dy0)

        // Second vertex.
        val a1 =
            (2 * (1 + 2 * UNSKEW_2D) * (1 / UNSKEW_2D + 2)).toFloat() * t + ((-2 * (1 + 2 * UNSKEW_2D) * (1 + 2 * UNSKEW_2D)).toFloat() + a0)
        val dx1 = dx0 - (1 + 2 * UNSKEW_2D).toFloat()
        val dy1 = dy0 - (1 + 2 * UNSKEW_2D).toFloat()
        value += (a1 * a1) * (a1 * a1) * grad(seed, xsbp + PRIME_X, ysbp + PRIME_Y, dx1, dy1)

        // Third and fourth vertices.
        // Nested conditionals were faster than compact bit logic/arithmetic.
        val xmyi = xi - yi
        if (t < UNSKEW_2D) {
            if (xi + xmyi > 1) {
                val dx2 = dx0 - (3 * UNSKEW_2D + 2).toFloat()
                val dy2 = dy0 - (3 * UNSKEW_2D + 1).toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp + (PRIME_X shl 1), ysbp + PRIME_Y, dx2, dy2)
                }
            } else {
                val dx2 = dx0 - UNSKEW_2D.toFloat()
                val dy2 = dy0 - (UNSKEW_2D + 1).toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp + PRIME_Y, dx2, dy2)
                }
            }

            if (yi - xmyi > 1) {
                val dx3 = dx0 - (3 * UNSKEW_2D + 1).toFloat()
                val dy3 = dy0 - (3 * UNSKEW_2D + 2).toFloat()
                val a3 = RSQUARED_2D - dx3 * dx3 - dy3 * dy3
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * grad(seed, xsbp + PRIME_X, ysbp + (PRIME_Y shl 1), dx3, dy3)
                }
            } else {
                val dx3 = dx0 - (UNSKEW_2D + 1).toFloat()
                val dy3 = dy0 - UNSKEW_2D.toFloat()
                val a3 = RSQUARED_2D - dx3 * dx3 - dy3 * dy3
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * grad(seed, xsbp + PRIME_X, ysbp, dx3, dy3)
                }
            }
        } else {
            if (xi + xmyi < 0) {
                val dx2 = dx0 + (1 + UNSKEW_2D).toFloat()
                val dy2 = dy0 + UNSKEW_2D.toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp - PRIME_X, ysbp, dx2, dy2)
                }
            } else {
                val dx2 = dx0 - (UNSKEW_2D + 1).toFloat()
                val dy2 = dy0 - UNSKEW_2D.toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp + PRIME_X, ysbp, dx2, dy2)
                }
            }

            if (yi < xmyi) {
                val dx2 = dx0 + UNSKEW_2D.toFloat()
                val dy2 = dy0 + (UNSKEW_2D + 1).toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp - PRIME_Y, dx2, dy2)
                }
            } else {
                val dx2 = dx0 - UNSKEW_2D.toFloat()
                val dy2 = dy0 - (UNSKEW_2D + 1).toFloat()
                val a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp + PRIME_Y, dx2, dy2)
                }
            }
        }

        return value
    }

    /**
     * 3D OpenSimplex2S/SuperSimplex noise, with better visual isotropy in (X, Y).
     * Recommended for 3D terrain and time-varied animations.
     * The Z coordinate should always be the "different" coordinate in whatever your use case is.
     * If Y is vertical in world coordinates, call noise3_ImproveXZ(x, z, Y) or use noise3_XZBeforeY.
     * If Z is vertical in world coordinates, call noise3_ImproveXZ(x, y, Z).
     * For a time varied animation, call noise3_ImproveXY(x, y, T).
     */
    fun noise3_ImproveXY(seed: Long, x: Double, y: Double, z: Double): Float {
        // Re-orient the cubic lattices without skewing, so Z points up the main lattice diagonal,
        // and the planes formed by XY are moved far out of alignment with the cube faces.
        // Orthonormal rotation. Not a skew transform.

        val xy = x + y
        val s2 = xy * ROTATE3_ORTHOGONALIZER
        val zz = z * ROOT3OVER3
        val xr = x + s2 + zz
        val yr = y + s2 + zz
        val zr = xy * -ROOT3OVER3 + zz

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr)
    }

    /**
     * 3D OpenSimplex2S/SuperSimplex noise, with better visual isotropy in (X, Z).
     * Recommended for 3D terrain and time-varied animations.
     * The Y coordinate should always be the "different" coordinate in whatever your use case is.
     * If Y is vertical in world coordinates, call noise3_ImproveXZ(x, Y, z).
     * If Z is vertical in world coordinates, call noise3_ImproveXZ(x, Z, y) or use noise3_ImproveXY.
     * For a time varied animation, call noise3_ImproveXZ(x, T, y) or use noise3_ImproveXY.
     */
    fun noise3_ImproveXZ(seed: Long, x: Double, y: Double, z: Double): Float {
        // Re-orient the cubic lattices without skewing, so Y points up the main lattice diagonal,
        // and the planes formed by XZ are moved far out of alignment with the cube faces.
        // Orthonormal rotation. Not a skew transform.

        val xz = x + z
        val s2 = xz * -0.211324865405187
        val yy = y * ROOT3OVER3
        val xr = x + s2 + yy
        val zr = z + s2 + yy
        val yr = xz * -ROOT3OVER3 + yy

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr)
    }

    /**
     * 3D OpenSimplex2S/SuperSimplex noise, fallback rotation option
     * Use noise3_ImproveXY or noise3_ImproveXZ instead, wherever appropriate.
     * They have less diagonal bias. This function's best use is as a fallback.
     */
    fun noise3_Fallback(seed: Long, x: Double, y: Double, z: Double): Float {
        // Re-orient the cubic lattices via rotation, to produce a familiar look.
        // Orthonormal rotation. Not a skew transform.

        val r = FALLBACK_ROTATE3 * (x + y + z)
        val xr = r - x
        val yr = r - y
        val zr = r - z

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr)
    }

    /**
     * Generate overlapping cubic lattices for 3D Re-oriented BCC noise.
     * Lookup table implementation inspired by DigitalShadow.
     * It was actually faster to narrow down the points in the loop itself,
     * than to build up the index with enough info to isolate 8 points.
     */
    private fun noise3_UnrotatedBase(seed: Long, xr: Double, yr: Double, zr: Double): Float {
        // Get base points and offsets.

        val xrb = fastFloor(xr)
        val yrb = fastFloor(yr)
        val zrb = fastFloor(zr)
        val xi = (xr - xrb).toFloat()
        val yi = (yr - yrb).toFloat()
        val zi = (zr - zrb).toFloat()

        // Prime pre-multiplication for hash. Also flip seed for second lattice copy.
        val xrbp = xrb * PRIME_X
        val yrbp = yrb * PRIME_Y
        val zrbp = zrb * PRIME_Z
        val seed2 = seed xor -0x52D547B2E96ED629L

        // -1 if positive, 0 if negative.
        val xNMask = (-0.5f - xi).toInt()
        val yNMask = (-0.5f - yi).toInt()
        val zNMask = (-0.5f - zi).toInt()

        // First vertex.
        val x0 = xi + xNMask
        val y0 = yi + yNMask
        val z0 = zi + zNMask
        val a0 = RSQUARED_3D - x0 * x0 - y0 * y0 - z0 * z0
        var value = (a0 * a0) * (a0 * a0) * grad(
            seed,
            xrbp + (xNMask.toLong() and PRIME_X),
            yrbp + (yNMask.toLong() and PRIME_Y),
            zrbp + (zNMask.toLong() and PRIME_Z),
            x0,
            y0,
            z0
        )

        // Second vertex.
        val x1 = xi - 0.5f
        val y1 = yi - 0.5f
        val z1 = zi - 0.5f
        val a1 = RSQUARED_3D - x1 * x1 - y1 * y1 - z1 * z1
        value += (a1 * a1) * (a1 * a1) * grad(
            seed2,
            xrbp + PRIME_X, yrbp + PRIME_Y, zrbp + PRIME_Z, x1, y1, z1
        )

        // Shortcuts for building the remaining falloffs.
        // Derived by subtracting the polynomials with the offsets plugged in.
        val xAFlipMask0 = ((xNMask or 1) shl 1) * x1
        val yAFlipMask0 = ((yNMask or 1) shl 1) * y1
        val zAFlipMask0 = ((zNMask or 1) shl 1) * z1
        val xAFlipMask1 = (-2 - (xNMask shl 2)) * x1 - 1.0f
        val yAFlipMask1 = (-2 - (yNMask shl 2)) * y1 - 1.0f
        val zAFlipMask1 = (-2 - (zNMask shl 2)) * z1 - 1.0f

        var skip5 = false
        val a2 = xAFlipMask0 + a0
        if (a2 > 0) {
            val x2 = x0 - (xNMask or 1)
            val y2 = y0
            val z2 = z0
            value += (a2 * a2) * (a2 * a2) * grad(
                seed,
                xrbp + (xNMask.toLong().inv() and PRIME_X),
                yrbp + (yNMask.toLong() and PRIME_Y),
                zrbp + (zNMask.toLong() and PRIME_Z),
                x2,
                y2,
                z2
            )
        } else {
            val a3 = yAFlipMask0 + zAFlipMask0 + a0
            if (a3 > 0) {
                val x3 = x0
                val y3 = y0 - (yNMask or 1)
                val z3 = z0 - (zNMask or 1)
                value += (a3 * a3) * (a3 * a3) * grad(
                    seed,
                    xrbp + (xNMask.toLong() and PRIME_X),
                    yrbp + (yNMask.toLong().inv() and PRIME_Y),
                    zrbp + (zNMask.toLong().inv() and PRIME_Z),
                    x3,
                    y3,
                    z3
                )
            }

            val a4 = xAFlipMask1 + a1
            if (a4 > 0) {
                val x4 = (xNMask or 1) + x1
                val y4 = y1
                val z4 = z1
                value += (a4 * a4) * (a4 * a4) * grad(
                    seed2,
                    xrbp + (xNMask.toLong() and (PRIME_X * 2)), yrbp + PRIME_Y, zrbp + PRIME_Z, x4, y4, z4
                )
                skip5 = true
            }
        }

        var skip9 = false
        val a6 = yAFlipMask0 + a0
        if (a6 > 0) {
            val x6 = x0
            val y6 = y0 - (yNMask or 1)
            val z6 = z0
            value += (a6 * a6) * (a6 * a6) * grad(
                seed,
                xrbp + (xNMask.toLong() and PRIME_X),
                yrbp + (yNMask.toLong().inv() and PRIME_Y),
                zrbp + (zNMask.toLong() and PRIME_Z),
                x6,
                y6,
                z6
            )
        } else {
            val a7 = xAFlipMask0 + zAFlipMask0 + a0
            if (a7 > 0) {
                val x7 = x0 - (xNMask or 1)
                val y7 = y0
                val z7 = z0 - (zNMask or 1)
                value += (a7 * a7) * (a7 * a7) * grad(
                    seed,
                    xrbp + (xNMask.toLong().inv() and PRIME_X),
                    yrbp + (yNMask.toLong() and PRIME_Y),
                    zrbp + (zNMask.toLong().inv() and PRIME_Z),
                    x7,
                    y7,
                    z7
                )
            }

            val a8 = yAFlipMask1 + a1
            if (a8 > 0) {
                val x8 = x1
                val y8 = (yNMask or 1) + y1
                val z8 = z1
                value += (a8 * a8) * (a8 * a8) * grad(
                    seed2,
                    xrbp + PRIME_X, yrbp + (yNMask.toLong() and (PRIME_Y shl 1)), zrbp + PRIME_Z, x8, y8, z8
                )
                skip9 = true
            }
        }

        var skipD = false
        val aA = zAFlipMask0 + a0
        if (aA > 0) {
            val xA = x0
            val yA = y0
            val zA = z0 - (zNMask or 1)
            value += (aA * aA) * (aA * aA) * grad(
                seed,
                xrbp + (xNMask.toLong() and PRIME_X),
                yrbp + (yNMask.toLong() and PRIME_Y),
                zrbp + (zNMask.toLong().inv() and PRIME_Z),
                xA,
                yA,
                zA
            )
        } else {
            val aB = xAFlipMask0 + yAFlipMask0 + a0
            if (aB > 0) {
                val xB = x0 - (xNMask or 1)
                val yB = y0 - (yNMask or 1)
                val zB = z0
                value += (aB * aB) * (aB * aB) * grad(
                    seed,
                    xrbp + (xNMask.toLong().inv() and PRIME_X),
                    yrbp + (yNMask.toLong().inv() and PRIME_Y),
                    zrbp + (zNMask.toLong() and PRIME_Z),
                    xB,
                    yB,
                    zB
                )
            }

            val aC = zAFlipMask1 + a1
            if (aC > 0) {
                val xC = x1
                val yC = y1
                val zC = (zNMask or 1) + z1
                value += (aC * aC) * (aC * aC) * grad(
                    seed2,
                    xrbp + PRIME_X, yrbp + PRIME_Y, zrbp + (zNMask.toLong() and (PRIME_Z shl 1)), xC, yC, zC
                )
                skipD = true
            }
        }

        if (!skip5) {
            val a5 = yAFlipMask1 + zAFlipMask1 + a1
            if (a5 > 0) {
                val x5 = x1
                val y5 = (yNMask or 1) + y1
                val z5 = (zNMask or 1) + z1
                value += (a5 * a5) * (a5 * a5) * grad(
                    seed2,
                    xrbp + PRIME_X,
                    yrbp + (yNMask.toLong() and (PRIME_Y shl 1)),
                    zrbp + (zNMask.toLong() and (PRIME_Z shl 1)),
                    x5,
                    y5,
                    z5
                )
            }
        }

        if (!skip9) {
            val a9 = xAFlipMask1 + zAFlipMask1 + a1
            if (a9 > 0) {
                val x9 = (xNMask or 1) + x1
                val y9 = y1
                val z9 = (zNMask or 1) + z1
                value += (a9 * a9) * (a9 * a9) * grad(
                    seed2,
                    xrbp + (xNMask.toLong() and (PRIME_X * 2)),
                    yrbp + PRIME_Y,
                    zrbp + (zNMask.toLong() and (PRIME_Z shl 1)),
                    x9,
                    y9,
                    z9
                )
            }
        }

        if (!skipD) {
            val aD = xAFlipMask1 + yAFlipMask1 + a1
            if (aD > 0) {
                val xD = (xNMask or 1) + x1
                val yD = (yNMask or 1) + y1
                val zD = z1
                value += (aD * aD) * (aD * aD) * grad(
                    seed2,
                    xrbp + (xNMask.toLong() and (PRIME_X shl 1)),
                    yrbp + (yNMask.toLong() and (PRIME_Y shl 1)),
                    zrbp + PRIME_Z,
                    xD,
                    yD,
                    zD
                )
            }
        }

        return value
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_ImproveXY
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Z is vertical
     */
    fun noise4_ImproveXYZ_ImproveXY(seed: Long, x: Double, y: Double, z: Double, w: Double): Float {
        val xy = x + y
        val s2 = xy * -0.21132486540518699998
        val zz = z * 0.28867513459481294226
        val ww = w * 1.118033988749894
        val xr = x + (zz + ww + s2)
        val yr = y + (zz + ww + s2)
        val zr = xy * -0.57735026918962599998 + (zz + ww)
        val wr = z * -0.866025403784439 + ww

        return noise4_UnskewedBase(seed, xr, yr, zr, wr)
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_ImproveXZ
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Y is vertical
     */
    fun noise4_ImproveXYZ_ImproveXZ(seed: Long, x: Double, y: Double, z: Double, w: Double): Float {
        val xz = x + z
        val s2 = xz * -0.21132486540518699998
        val yy = y * 0.28867513459481294226
        val ww = w * 1.118033988749894
        val xr = x + (yy + ww + s2)
        val zr = z + (yy + ww + s2)
        val yr = xz * -0.57735026918962599998 + (yy + ww)
        val wr = y * -0.866025403784439 + ww

        return noise4_UnskewedBase(seed, xr, yr, zr, wr)
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_Fallback
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * where there isn't a clear distinction between horizontal and vertical
     */
    fun noise4_ImproveXYZ(seed: Long, x: Double, y: Double, z: Double, w: Double): Float {
        val xyz = x + y + z
        val ww = w * 1.118033988749894
        val s2 = xyz * -0.16666666666666666 + ww
        val xs = x + s2
        val ys = y + s2
        val zs = z + s2
        val ws = -0.5 * xyz + ww

        return noise4_UnskewedBase(seed, xs, ys, zs, ws)
    }

    /**
     * 4D SuperSimplex noise, with XY and ZW forming orthogonal triangular-based planes.
     * Recommended for 3D terrain, where X and Y (or Z and W) are horizontal.
     * Recommended for noise(x, y, sin(time), cos(time)) trick.
     */
    fun noise4_ImproveXY_ImproveZW(seed: Long, x: Double, y: Double, z: Double, w: Double): Float {
        val s2 = (x + y) * -0.28522513987434876941 + (z + w) * 0.83897065470611435718
        val t2 = (z + w) * 0.21939749883706435719 + (x + y) * -0.48214856493302476942
        val xs = x + s2
        val ys = y + s2
        val zs = z + t2
        val ws = w + t2

        return noise4_UnskewedBase(seed, xs, ys, zs, ws)
    }

    /**
     * 4D SuperSimplex noise, fallback lattice orientation.
     */
    fun noise4_Fallback(seed: Long, x: Double, y: Double, z: Double, w: Double): Float {
        // Get points for A4 lattice

        val s = SKEW_4D * (x + y + z + w)
        val xs = x + s
        val ys = y + s
        val zs = z + s
        val ws = w + s

        return noise4_UnskewedBase(seed, xs, ys, zs, ws)
    }

    /**
     * 4D SuperSimplex noise base.
     * Using ultra-simple 4x4x4x4 lookup partitioning.
     * This isn't as elegant or SIMD/GPU/etc. portable as other approaches,
     * but it competes performance-wise with optimized 2014 OpenSimplex.
     */
    private fun noise4_UnskewedBase(seed: Long, xs: Double, ys: Double, zs: Double, ws: Double): Float {
        // Get base points and offsets

        val xsb = fastFloor(xs)
        val ysb = fastFloor(ys)
        val zsb = fastFloor(zs)
        val wsb = fastFloor(ws)
        val xsi = (xs - xsb).toFloat()
        val ysi = (ys - ysb).toFloat()
        val zsi = (zs - zsb).toFloat()
        val wsi = (ws - wsb).toFloat()

        // Unskewed offsets
        val ssi = (xsi + ysi + zsi + wsi) * UNSKEW_4D
        val xi = xsi + ssi
        val yi = ysi + ssi
        val zi = zsi + ssi
        val wi = wsi + ssi

        // Prime pre-multiplication for hash.
        val xsvp = xsb * PRIME_X
        val ysvp = ysb * PRIME_Y
        val zsvp = zsb * PRIME_Z
        val wsvp = wsb * PRIME_W

        // Index into initial table.
        val index = (((fastFloor(xs * 4) and 3) shl 0)
                or ((fastFloor(ys * 4) and 3) shl 2)
                or ((fastFloor(zs * 4) and 3) shl 4)
                or ((fastFloor(ws * 4) and 3) shl 6))

        // Point contributions
        var value = 0f
        val secondaryIndexStartAndStop = LOOKUP_4D_A[index]
        val secondaryIndexStart = secondaryIndexStartAndStop and 0xFFFF
        val secondaryIndexStop = secondaryIndexStartAndStop shr 16
        for (i in secondaryIndexStart until secondaryIndexStop) {
            val c = LOOKUP_4D_B[i]
            val dx = xi + c!!.dx
            val dy = yi + c.dy
            val dz = zi + c.dz
            val dw = wi + c.dw
            var a = (dx * dx + dy * dy) + (dz * dz + dw * dw)
            if (a < RSQUARED_4D) {
                a -= RSQUARED_4D
                a *= a
                value += a * a * grad(seed, xsvp + c.xsvp, ysvp + c.ysvp, zsvp + c.zsvp, wsvp + c.wsvp, dx, dy, dz, dw)
            }
        }
        return value
    }

    /*
     * Utility
     */
    private fun grad(seed: Long, xsvp: Long, ysvp: Long, dx: Float, dy: Float): Float {
        var hash = seed xor xsvp xor ysvp
        hash *= HASH_MULTIPLIER
        hash = hash xor (hash shr (64 - N_GRADS_2D_EXPONENT + 1))
        val gi = hash.toInt() and ((N_GRADS_2D - 1) shl 1)
        return GRADIENTS_2D[gi or 0] * dx + GRADIENTS_2D[gi or 1] * dy
    }

    private fun grad(seed: Long, xrvp: Long, yrvp: Long, zrvp: Long, dx: Float, dy: Float, dz: Float): Float {
        var hash = (seed xor xrvp) xor (yrvp xor zrvp)
        hash *= HASH_MULTIPLIER
        hash = hash xor (hash shr (64 - N_GRADS_3D_EXPONENT + 2))
        val gi = hash.toInt() and ((N_GRADS_3D - 1) shl 2)
        return GRADIENTS_3D[gi or 0] * dx + GRADIENTS_3D[gi or 1] * dy + GRADIENTS_3D[gi or 2] * dz
    }

    private fun grad(
        seed: Long,
        xsvp: Long,
        ysvp: Long,
        zsvp: Long,
        wsvp: Long,
        dx: Float,
        dy: Float,
        dz: Float,
        dw: Float
    ): Float {
        var hash = seed xor (xsvp xor ysvp) xor (zsvp xor wsvp)
        hash *= HASH_MULTIPLIER
        hash = hash xor (hash shr (64 - N_GRADS_4D_EXPONENT + 2))
        val gi = hash.toInt() and ((N_GRADS_4D - 1) shl 2)
        return (GRADIENTS_4D[gi or 0] * dx + GRADIENTS_4D[gi or 1] * dy) + (GRADIENTS_4D[gi or 2] * dz + GRADIENTS_4D[gi or 3] * dw)
    }

    private fun fastFloor(x: Double): Int {
        val xi = x.toInt()
        return if (x < xi) xi - 1 else xi
    }

    /*
     * Lookup Tables & Gradients
     */
    private var GRADIENTS_2D = FloatArray(N_GRADS_2D * 2)
    private var GRADIENTS_3D: FloatArray
    private var GRADIENTS_4D: FloatArray
    private var LOOKUP_4D_A: IntArray
    private var LOOKUP_4D_B: Array<LatticeVertex4D?>

    init {
        val grad2 = floatArrayOf(
            0.38268343236509f, 0.923879532511287f,
            0.923879532511287f, 0.38268343236509f,
            0.923879532511287f, -0.38268343236509f,
            0.38268343236509f, -0.923879532511287f,
            -0.38268343236509f, -0.923879532511287f,
            -0.923879532511287f, -0.38268343236509f,
            -0.923879532511287f, 0.38268343236509f,
            -0.38268343236509f, 0.923879532511287f,  //-------------------------------------//
            0.130526192220052f, 0.99144486137381f,
            0.608761429008721f, 0.793353340291235f,
            0.793353340291235f, 0.608761429008721f,
            0.99144486137381f, 0.130526192220051f,
            0.99144486137381f, -0.130526192220051f,
            0.793353340291235f, -0.60876142900872f,
            0.608761429008721f, -0.793353340291235f,
            0.130526192220052f, -0.99144486137381f,
            -0.130526192220052f, -0.99144486137381f,
            -0.608761429008721f, -0.793353340291235f,
            -0.793353340291235f, -0.608761429008721f,
            -0.99144486137381f, -0.130526192220052f,
            -0.99144486137381f, 0.130526192220051f,
            -0.793353340291235f, 0.608761429008721f,
            -0.608761429008721f, 0.793353340291235f,
            -0.130526192220052f, 0.99144486137381f,
        )
        for (i in grad2.indices) {
            grad2[i] = (grad2[i] / NORMALIZER_2D).toFloat()
        }
        run {
            var i = 0
            var j = 0
            while (i < GRADIENTS_2D.size) {
                if (j == grad2.size) j = 0
                GRADIENTS_2D[i] = grad2[j]
                i++
                j++
            }
        }

        GRADIENTS_3D = FloatArray(N_GRADS_3D * 4)
        val grad3 = floatArrayOf(
            2.22474487139f,
            2.22474487139f,
            -1.0f,
            0.0f,
            2.22474487139f,
            2.22474487139f,
            1.0f,
            0.0f,
            3.0862664687972017f,
            1.1721513422464978f,
            0.0f,
            0.0f,
            1.1721513422464978f,
            3.0862664687972017f,
            0.0f,
            0.0f,
            -2.22474487139f,
            2.22474487139f,
            -1.0f,
            0.0f,
            -2.22474487139f,
            2.22474487139f,
            1.0f,
            0.0f,
            -1.1721513422464978f,
            3.0862664687972017f,
            0.0f,
            0.0f,
            -3.0862664687972017f,
            1.1721513422464978f,
            0.0f,
            0.0f,
            -1.0f,
            -2.22474487139f,
            -2.22474487139f,
            0.0f,
            1.0f,
            -2.22474487139f,
            -2.22474487139f,
            0.0f,
            0.0f,
            -3.0862664687972017f,
            -1.1721513422464978f,
            0.0f,
            0.0f,
            -1.1721513422464978f,
            -3.0862664687972017f,
            0.0f,
            -1.0f,
            -2.22474487139f,
            2.22474487139f,
            0.0f,
            1.0f,
            -2.22474487139f,
            2.22474487139f,
            0.0f,
            0.0f,
            -1.1721513422464978f,
            3.0862664687972017f,
            0.0f,
            0.0f,
            -3.0862664687972017f,
            1.1721513422464978f,
            0.0f,  //--------------------------------------------------------------------//
            -2.22474487139f,
            -2.22474487139f,
            -1.0f,
            0.0f,
            -2.22474487139f,
            -2.22474487139f,
            1.0f,
            0.0f,
            -3.0862664687972017f,
            -1.1721513422464978f,
            0.0f,
            0.0f,
            -1.1721513422464978f,
            -3.0862664687972017f,
            0.0f,
            0.0f,
            -2.22474487139f,
            -1.0f,
            -2.22474487139f,
            0.0f,
            -2.22474487139f,
            1.0f,
            -2.22474487139f,
            0.0f,
            -1.1721513422464978f,
            0.0f,
            -3.0862664687972017f,
            0.0f,
            -3.0862664687972017f,
            0.0f,
            -1.1721513422464978f,
            0.0f,
            -2.22474487139f,
            -1.0f,
            2.22474487139f,
            0.0f,
            -2.22474487139f,
            1.0f,
            2.22474487139f,
            0.0f,
            -3.0862664687972017f,
            0.0f,
            1.1721513422464978f,
            0.0f,
            -1.1721513422464978f,
            0.0f,
            3.0862664687972017f,
            0.0f,
            -1.0f,
            2.22474487139f,
            -2.22474487139f,
            0.0f,
            1.0f,
            2.22474487139f,
            -2.22474487139f,
            0.0f,
            0.0f,
            1.1721513422464978f,
            -3.0862664687972017f,
            0.0f,
            0.0f,
            3.0862664687972017f,
            -1.1721513422464978f,
            0.0f,
            -1.0f,
            2.22474487139f,
            2.22474487139f,
            0.0f,
            1.0f,
            2.22474487139f,
            2.22474487139f,
            0.0f,
            0.0f,
            3.0862664687972017f,
            1.1721513422464978f,
            0.0f,
            0.0f,
            1.1721513422464978f,
            3.0862664687972017f,
            0.0f,
            2.22474487139f,
            -2.22474487139f,
            -1.0f,
            0.0f,
            2.22474487139f,
            -2.22474487139f,
            1.0f,
            0.0f,
            1.1721513422464978f,
            -3.0862664687972017f,
            0.0f,
            0.0f,
            3.0862664687972017f,
            -1.1721513422464978f,
            0.0f,
            0.0f,
            2.22474487139f,
            -1.0f,
            -2.22474487139f,
            0.0f,
            2.22474487139f,
            1.0f,
            -2.22474487139f,
            0.0f,
            3.0862664687972017f,
            0.0f,
            -1.1721513422464978f,
            0.0f,
            1.1721513422464978f,
            0.0f,
            -3.0862664687972017f,
            0.0f,
            2.22474487139f,
            -1.0f,
            2.22474487139f,
            0.0f,
            2.22474487139f,
            1.0f,
            2.22474487139f,
            0.0f,
            1.1721513422464978f,
            0.0f,
            3.0862664687972017f,
            0.0f,
            3.0862664687972017f,
            0.0f,
            1.1721513422464978f,
            0.0f,
        )
        for (i in grad3.indices) {
            grad3[i] = (grad3[i] / NORMALIZER_3D).toFloat()
        }
        run {
            var i = 0
            var j = 0
            while (i < GRADIENTS_3D.size) {
                if (j == grad3.size) j = 0
                GRADIENTS_3D[i] = grad3[j]
                i++
                j++
            }
        }

        GRADIENTS_4D = FloatArray(N_GRADS_4D * 4)
        val grad4 = floatArrayOf(
            -0.6740059517812944f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.7504883828755602f,
            0.15296486218853164f,
            -0.4004672082940195f,
            0.5029860367700724f,
            -0.8828161875373585f,
            0.08164729285680945f,
            0.08164729285680945f,
            0.4553054119602712f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.5029860367700724f,
            0.4004672082940195f,
            -0.15296486218853164f,
            0.7504883828755602f,
            -0.5794684678643381f,
            0.3239847771997537f,
            0.3239847771997537f,
            0.6740059517812944f,
            -0.6740059517812944f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.4004672082940195f,
            -0.8828161875373585f,
            0.08164729285680945f,
            0.4553054119602712f,
            0.08164729285680945f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.15296486218853164f,
            -0.5794684678643381f,
            0.3239847771997537f,
            0.6740059517812944f,
            0.3239847771997537f,
            -0.6740059517812944f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            -0.7504883828755602f,
            0.5029860367700724f,
            -0.4004672082940195f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.4004672082940195f,
            -0.8828161875373585f,
            0.4553054119602712f,
            0.08164729285680945f,
            0.08164729285680945f,
            -0.4553054119602712f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            -0.5029860367700724f,
            0.7504883828755602f,
            -0.15296486218853164f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.15296486218853164f,
            -0.5794684678643381f,
            0.6740059517812944f,
            0.3239847771997537f,
            0.3239847771997537f,
            0.5794684678643381f,
            -0.6740059517812944f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            0.5029860367700724f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.7504883828755602f,
            0.15296486218853164f,
            -0.4004672082940195f,
            0.4553054119602712f,
            -0.8828161875373585f,
            0.08164729285680945f,
            0.08164729285680945f,
            0.8828161875373585f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            0.7504883828755602f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.5029860367700724f,
            0.4004672082940195f,
            -0.15296486218853164f,
            0.6740059517812944f,
            -0.5794684678643381f,
            0.3239847771997537f,
            0.3239847771997537f,  //------------------------------------------------------------------------------------------//
            -0.753341017856078f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            -0.8586508742123365f,
            -0.508629699630796f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            -0.508629699630796f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.508629699630796f,
            -0.9982828964265062f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.37968289875261624f,
            -0.753341017856078f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            0.12128480194602098f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            -0.508629699630796f,
            -0.8586508742123365f,
            0.044802370851755174f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.8586508742123365f,
            -0.508629699630796f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            -0.508629699630796f,
            -0.03381941603233842f,
            -0.9982828964265062f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.753341017856078f,
            -0.37968289875261624f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            0.12128480194602098f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            -0.508629699630796f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.508629699630796f,
            -0.8586508742123365f,
            0.044802370851755174f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.8586508742123365f,
            -0.508629699630796f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.9982828964265062f,
            -0.03381941603233842f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.37968289875261624f,
            -0.753341017856078f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.7821684431180708f,
            -0.4321472685365301f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            0.12128480194602098f,
            -0.4321472685365301f,
            -0.4321472685365301f,
            -0.7821684431180708f,
            -0.508629699630796f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            -0.508629699630796f,
            0.044802370851755174f,
            -0.8586508742123365f,
            0.044802370851755174f,
            0.044802370851755174f,
            -0.508629699630796f,
            -0.8586508742123365f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.03381941603233842f,
            -0.9982828964265062f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.15296486218853164f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.5029860367700724f,
            0.08164729285680945f,
            -0.8828161875373585f,
            0.08164729285680945f,
            0.4553054119602712f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.4004672082940195f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.7504883828755602f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.3239847771997537f,
            0.6740059517812944f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            0.5794684678643381f,
            -0.4004672082940195f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.5029860367700724f,
            0.08164729285680945f,
            0.08164729285680945f,
            -0.8828161875373585f,
            0.4553054119602712f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            0.8828161875373585f,
            -0.15296486218853164f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.7504883828755602f,
            0.3239847771997537f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.6740059517812944f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.5029860367700724f,
            0.15296486218853164f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.5029860367700724f,
            -0.4004672082940195f,
            0.08164729285680945f,
            -0.8828161875373585f,
            0.4553054119602712f,
            0.08164729285680945f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.7504883828755602f,
            0.4004672082940195f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.7504883828755602f,
            -0.15296486218853164f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.6740059517812944f,
            0.3239847771997537f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.6740059517812944f,
            -0.4004672082940195f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.7504883828755602f,
            0.15296486218853164f,
            -0.4004672082940195f,
            0.5029860367700724f,
            -0.7504883828755602f,
            0.08164729285680945f,
            0.08164729285680945f,
            0.4553054119602712f,
            -0.8828161875373585f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.4553054119602712f,
            -0.15296486218853164f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.5029860367700724f,
            0.4004672082940195f,
            -0.15296486218853164f,
            0.7504883828755602f,
            -0.5029860367700724f,
            0.3239847771997537f,
            0.3239847771997537f,
            0.6740059517812944f,
            -0.5794684678643381f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.6740059517812944f,
            -0.3239847771997537f,
            -0.4004672082940195f,
            0.5029860367700724f,
            -0.7504883828755602f,
            0.15296486218853164f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.08164729285680945f,
            0.4553054119602712f,
            -0.8828161875373585f,
            0.08164729285680945f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            -0.15296486218853164f,
            0.7504883828755602f,
            -0.5029860367700724f,
            0.4004672082940195f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.3239847771997537f,
            0.6740059517812944f,
            -0.5794684678643381f,
            0.3239847771997537f,
            -0.3239847771997537f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            -0.4004672082940195f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.15296486218853164f,
            0.5029860367700724f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.08164729285680945f,
            0.4553054119602712f,
            0.08164729285680945f,
            -0.8828161875373585f,
            -0.08164729285680945f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            -0.15296486218853164f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.4004672082940195f,
            0.7504883828755602f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.3239847771997537f,
            0.6740059517812944f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            -0.3239847771997537f,
            0.5029860367700724f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.15296486218853164f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.7504883828755602f,
            -0.4004672082940195f,
            0.4553054119602712f,
            0.08164729285680945f,
            -0.8828161875373585f,
            0.08164729285680945f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            -0.08164729285680945f,
            0.7504883828755602f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.4004672082940195f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.5029860367700724f,
            -0.15296486218853164f,
            0.6740059517812944f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.3239847771997537f,
            0.5794684678643381f,
            -0.3239847771997537f,
            -0.3239847771997537f,
            -0.6740059517812944f,
            0.5029860367700724f,
            -0.4004672082940195f,
            0.15296486218853164f,
            -0.7504883828755602f,
            0.5029860367700724f,
            0.15296486218853164f,
            -0.4004672082940195f,
            -0.7504883828755602f,
            0.4553054119602712f,
            0.08164729285680945f,
            0.08164729285680945f,
            -0.8828161875373585f,
            0.8828161875373585f,
            -0.08164729285680945f,
            -0.08164729285680945f,
            -0.4553054119602712f,
            0.7504883828755602f,
            -0.15296486218853164f,
            0.4004672082940195f,
            -0.5029860367700724f,
            0.7504883828755602f,
            0.4004672082940195f,
            -0.15296486218853164f,
            -0.5029860367700724f,
            0.6740059517812944f,
            0.3239847771997537f,
            0.3239847771997537f,
            -0.5794684678643381f,
            0.03381941603233842f,
            0.03381941603233842f,
            0.03381941603233842f,
            0.9982828964265062f,
            -0.044802370851755174f,
            -0.044802370851755174f,
            0.508629699630796f,
            0.8586508742123365f,
            -0.044802370851755174f,
            0.508629699630796f,
            -0.044802370851755174f,
            0.8586508742123365f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.7821684431180708f,
            0.508629699630796f,
            -0.044802370851755174f,
            -0.044802370851755174f,
            0.8586508742123365f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.7821684431180708f,
            0.4321472685365301f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.7821684431180708f,
            0.37968289875261624f,
            0.37968289875261624f,
            0.37968289875261624f,
            0.753341017856078f,
            0.03381941603233842f,
            0.03381941603233842f,
            0.9982828964265062f,
            0.03381941603233842f,
            -0.044802370851755174f,
            0.044802370851755174f,
            0.8586508742123365f,
            0.508629699630796f,
            -0.044802370851755174f,
            0.508629699630796f,
            0.8586508742123365f,
            -0.044802370851755174f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.7821684431180708f,
            0.4321472685365301f,
            0.508629699630796f,
            -0.044802370851755174f,
            0.8586508742123365f,
            -0.044802370851755174f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.7821684431180708f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.7821684431180708f,
            -0.12128480194602098f,
            0.37968289875261624f,
            0.37968289875261624f,
            0.753341017856078f,
            0.37968289875261624f,
            0.03381941603233842f,
            0.9982828964265062f,
            0.03381941603233842f,
            0.03381941603233842f,
            -0.044802370851755174f,
            0.8586508742123365f,
            -0.044802370851755174f,
            0.508629699630796f,
            -0.044802370851755174f,
            0.8586508742123365f,
            0.508629699630796f,
            -0.044802370851755174f,
            -0.12128480194602098f,
            0.7821684431180708f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.508629699630796f,
            0.8586508742123365f,
            -0.044802370851755174f,
            -0.044802370851755174f,
            0.4321472685365301f,
            0.7821684431180708f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.7821684431180708f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.37968289875261624f,
            0.753341017856078f,
            0.37968289875261624f,
            0.37968289875261624f,
            0.9982828964265062f,
            0.03381941603233842f,
            0.03381941603233842f,
            0.03381941603233842f,
            0.8586508742123365f,
            -0.044802370851755174f,
            -0.044802370851755174f,
            0.508629699630796f,
            0.8586508742123365f,
            -0.044802370851755174f,
            0.508629699630796f,
            -0.044802370851755174f,
            0.7821684431180708f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.4321472685365301f,
            0.8586508742123365f,
            0.508629699630796f,
            -0.044802370851755174f,
            -0.044802370851755174f,
            0.7821684431180708f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.4321472685365301f,
            0.7821684431180708f,
            0.4321472685365301f,
            0.4321472685365301f,
            -0.12128480194602098f,
            0.753341017856078f,
            0.37968289875261624f,
            0.37968289875261624f,
            0.37968289875261624f,
        )
        for (i in grad4.indices) {
            grad4[i] = (grad4[i] / NORMALIZER_4D).toFloat()
        }
        run {
            var i = 0
            var j = 0
            while (i < GRADIENTS_4D.size) {
                if (j == grad4.size) j = 0
                GRADIENTS_4D[i] = grad4[j]
                i++
                j++
            }
        }

        val lookup4DVertexCodes = arrayOf(
            intArrayOf(
                0x15,
                0x45,
                0x51,
                0x54,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA
            ),
            intArrayOf(0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA),
            intArrayOf(0x01, 0x05, 0x11, 0x15, 0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA),
            intArrayOf(
                0x01,
                0x15,
                0x16,
                0x45,
                0x46,
                0x51,
                0x52,
                0x55,
                0x56,
                0x5A,
                0x66,
                0x6A,
                0x96,
                0x9A,
                0xA6,
                0xAA,
                0xAB
            ),
            intArrayOf(0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA),
            intArrayOf(0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA),
            intArrayOf(0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA),
            intArrayOf(0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA, 0xAB),
            intArrayOf(0x04, 0x05, 0x14, 0x15, 0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA),
            intArrayOf(0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA),
            intArrayOf(0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA),
            intArrayOf(0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x5B, 0x6A, 0x9A, 0xAA, 0xAB),
            intArrayOf(
                0x04,
                0x15,
                0x19,
                0x45,
                0x49,
                0x54,
                0x55,
                0x58,
                0x59,
                0x5A,
                0x69,
                0x6A,
                0x99,
                0x9A,
                0xA9,
                0xAA,
                0xAE
            ),
            intArrayOf(0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA, 0xAE),
            intArrayOf(0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x5E, 0x6A, 0x9A, 0xAA, 0xAE),
            intArrayOf(
                0x05,
                0x15,
                0x1A,
                0x45,
                0x4A,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x5B,
                0x5E,
                0x6A,
                0x9A,
                0xAA,
                0xAB,
                0xAE,
                0xAF
            ),
            intArrayOf(0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x11, 0x15, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x11, 0x15, 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA),
            intArrayOf(0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x15, 0x16, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x6B, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xAA),
            intArrayOf(0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x6B, 0x9A, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x19, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x9A, 0xAA, 0xAE),
            intArrayOf(0x15, 0x1A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x10, 0x11, 0x14, 0x15, 0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA),
            intArrayOf(0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x67, 0x6A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA),
            intArrayOf(0x15, 0x16, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x6B, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA),
            intArrayOf(0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0xAA, 0xAB),
            intArrayOf(0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x6D, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x19, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x6E, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0xAA, 0xAE),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(
                0x10,
                0x15,
                0x25,
                0x51,
                0x54,
                0x55,
                0x61,
                0x64,
                0x65,
                0x66,
                0x69,
                0x6A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xBA
            ),
            intArrayOf(0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA, 0xBA),
            intArrayOf(0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0x76, 0xA6, 0xAA, 0xBA),
            intArrayOf(
                0x11,
                0x15,
                0x26,
                0x51,
                0x55,
                0x56,
                0x62,
                0x65,
                0x66,
                0x67,
                0x6A,
                0x76,
                0xA6,
                0xAA,
                0xAB,
                0xBA,
                0xBB
            ),
            intArrayOf(0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x25, 0x55, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x25, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA6, 0xAA, 0xBA),
            intArrayOf(0x15, 0x26, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0x79, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x25, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x25, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xAA, 0xBA),
            intArrayOf(0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(
                0x14,
                0x15,
                0x29,
                0x54,
                0x55,
                0x59,
                0x65,
                0x68,
                0x69,
                0x6A,
                0x6D,
                0x79,
                0xA9,
                0xAA,
                0xAE,
                0xBA,
                0xBE
            ),
            intArrayOf(0x15, 0x29, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(
                0x15,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x6B,
                0x6E,
                0x7A,
                0xAA,
                0xAB,
                0xAE,
                0xBA,
                0xBF
            ),
            intArrayOf(0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA),
            intArrayOf(0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x45, 0x46, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA),
            intArrayOf(0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x9A, 0x9B, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x49, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x99, 0x9A, 0x9E, 0xAA, 0xAE),
            intArrayOf(0x45, 0x4A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x59, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x56, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(
                0x15,
                0x45,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA6,
                0xA9,
                0xAA,
                0xAB,
                0xAE
            ),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE),
            intArrayOf(0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xA7, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(
                0x15,
                0x51,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xAB,
                0xBA
            ),
            intArrayOf(0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA),
            intArrayOf(
                0x15,
                0x54,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xAE,
                0xBA
            ),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAD, 0xAE),
            intArrayOf(0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x51, 0x55, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x62, 0x65, 0x66, 0x6A, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA),
            intArrayOf(0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA),
            intArrayOf(0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x68, 0x69, 0x6A, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE),
            intArrayOf(0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA, 0xAB, 0xAE, 0xBA, 0xBF),
            intArrayOf(0x40, 0x41, 0x44, 0x45, 0x50, 0x51, 0x54, 0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA),
            intArrayOf(0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x95, 0x96, 0x97, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA),
            intArrayOf(0x45, 0x46, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA),
            intArrayOf(0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xAA, 0xAB),
            intArrayOf(0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x95, 0x99, 0x9A, 0x9D, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x49, 0x55, 0x59, 0x5A, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xAA, 0xAE),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x51, 0x52, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(
                0x45,
                0x51,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xAB,
                0xEA
            ),
            intArrayOf(0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA),
            intArrayOf(
                0x45,
                0x54,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xAE,
                0xEA
            ),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x58, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE),
            intArrayOf(0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA),
            intArrayOf(0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(
                0x51,
                0x54,
                0x55,
                0x56,
                0x59,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xBA,
                0xEA
            ),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB),
            intArrayOf(0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE),
            intArrayOf(0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xAF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB5, 0xBA),
            intArrayOf(0x51, 0x55, 0x61, 0x65, 0x66, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x64, 0x65, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA),
            intArrayOf(0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA),
            intArrayOf(0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xBB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xBA),
            intArrayOf(
                0x40,
                0x45,
                0x51,
                0x54,
                0x55,
                0x85,
                0x91,
                0x94,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xEA
            ),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA, 0xEA),
            intArrayOf(0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xD6, 0xEA),
            intArrayOf(
                0x41,
                0x45,
                0x51,
                0x55,
                0x56,
                0x86,
                0x92,
                0x95,
                0x96,
                0x97,
                0x9A,
                0xA6,
                0xAA,
                0xAB,
                0xD6,
                0xEA,
                0xEB
            ),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x45, 0x55, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xDA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xDA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x86, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB),
            intArrayOf(0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xD9, 0xEA),
            intArrayOf(0x45, 0x55, 0x59, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xDA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xDA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB),
            intArrayOf(
                0x44,
                0x45,
                0x54,
                0x55,
                0x59,
                0x89,
                0x95,
                0x98,
                0x99,
                0x9A,
                0x9D,
                0xA9,
                0xAA,
                0xAE,
                0xD9,
                0xEA,
                0xEE
            ),
            intArrayOf(0x45, 0x55, 0x59, 0x89, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE),
            intArrayOf(0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE),
            intArrayOf(
                0x45,
                0x55,
                0x56,
                0x59,
                0x5A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0x9B,
                0x9E,
                0xAA,
                0xAB,
                0xAE,
                0xDA,
                0xEA,
                0xEF
            ),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x51, 0x55, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x91, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x92, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA),
            intArrayOf(0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x59, 0x94, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA),
            intArrayOf(0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x59, 0x95, 0x98, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE),
            intArrayOf(0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE),
            intArrayOf(0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xEA, 0xEF),
            intArrayOf(0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xE5, 0xEA),
            intArrayOf(0x51, 0x55, 0x65, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x65, 0x94, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA),
            intArrayOf(0x55, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x94, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA),
            intArrayOf(0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x55, 0x56, 0x59, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA),
            intArrayOf(0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xEA, 0xEB),
            intArrayOf(0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE),
            intArrayOf(0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE),
            intArrayOf(0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE),
            intArrayOf(0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xEA),
            intArrayOf(
                0x50,
                0x51,
                0x54,
                0x55,
                0x65,
                0x95,
                0xA1,
                0xA4,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xB5,
                0xBA,
                0xE5,
                0xEA,
                0xFA
            ),
            intArrayOf(0x51, 0x55, 0x65, 0x95, 0xA1, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA),
            intArrayOf(0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA),
            intArrayOf(
                0x51,
                0x55,
                0x56,
                0x65,
                0x66,
                0x95,
                0x96,
                0xA5,
                0xA6,
                0xA7,
                0xAA,
                0xAB,
                0xB6,
                0xBA,
                0xE6,
                0xEA,
                0xFB
            ),
            intArrayOf(0x54, 0x55, 0x65, 0x95, 0xA4, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA),
            intArrayOf(0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA),
            intArrayOf(0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA),
            intArrayOf(0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xEA, 0xFB),
            intArrayOf(0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA),
            intArrayOf(0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA),
            intArrayOf(0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA),
            intArrayOf(0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xEA),
            intArrayOf(
                0x54,
                0x55,
                0x59,
                0x65,
                0x69,
                0x95,
                0x99,
                0xA5,
                0xA9,
                0xAA,
                0xAD,
                0xAE,
                0xB9,
                0xBA,
                0xE9,
                0xEA,
                0xFE
            ),
            intArrayOf(0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA, 0xFE),
            intArrayOf(0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA),
            intArrayOf(
                0x55,
                0x56,
                0x59,
                0x5A,
                0x65,
                0x66,
                0x69,
                0x6A,
                0x95,
                0x96,
                0x99,
                0x9A,
                0xA5,
                0xA6,
                0xA9,
                0xAA,
                0xAB,
                0xAE,
                0xBA,
                0xEA
            ),
        )
        val latticeVerticesByCode = arrayOfNulls<LatticeVertex4D>(256)
        for (i in 0..255) {
            val cx = ((i shr 0) and 3) - 1
            val cy = ((i shr 2) and 3) - 1
            val cz = ((i shr 4) and 3) - 1
            val cw = ((i shr 6) and 3) - 1
            latticeVerticesByCode[i] = LatticeVertex4D(cx, cy, cz, cw)
        }
        var nLatticeVerticesTotal = 0
        for (i in 0..255) {
            nLatticeVerticesTotal += lookup4DVertexCodes[i].size
        }
        LOOKUP_4D_A = IntArray(256)
        LOOKUP_4D_B = arrayOfNulls(nLatticeVerticesTotal)
        var i = 0
        var j = 0
        while (i < 256) {
            LOOKUP_4D_A[i] = j or ((j + lookup4DVertexCodes[i].size) shl 16)
            for (k in lookup4DVertexCodes[i].indices) {
                LOOKUP_4D_B[j++] = latticeVerticesByCode[lookup4DVertexCodes[i][k]]
            }
            i++
        }
    }

    private class LatticeVertex4D(xsv: Int, ysv: Int, zsv: Int, wsv: Int) {
        val dx: Float
        val dy: Float
        val dz: Float
        val dw: Float
        val xsvp: Long = xsv * PRIME_X
        val ysvp: Long = ysv * PRIME_Y
        val zsvp: Long = zsv * PRIME_Z
        val wsvp: Long = wsv * PRIME_W

        init {
            val ssv = (xsv + ysv + zsv + wsv) * UNSKEW_4D
            this.dx = -xsv - ssv
            this.dy = -ysv - ssv
            this.dz = -zsv - ssv
            this.dw = -wsv - ssv
        }
    }
}