package com.murphy.avrecordsample.component.encoder.gles

import com.murphy.avrecordsample.utils.OpenGlUtils
import java.nio.FloatBuffer

class DrawStuff {

    private val FULL_RECTANGLE_COORDS = floatArrayOf(
            -1.0f, -1.0f, // 0 bottom left
            1.0f, -1.0f, // 1 bottom right
            -1.0f, 1.0f, // 2 top left
            1.0f, 1.0f)// 3 top right

    private val FULL_RECTANGLE_TEX_COORDS_RECORD = floatArrayOf(
            0.0f, 0.0f, // 0 bottom left
            1.0f, 0.0f, // 1 bottom right
            0.0f, 1.0f, // 2 top left
            1.0f, 1.0f      // 3 top right
    )

    private val FULL_RECTANGLE_TEX_COORDS_PREVIEW = floatArrayOf(
            1.0f, 0.0f, // 0 bottom left
            1.0f, 1.0f, // 1 bottom right
            0.0f, 0.0f, // 2 top left
            0.0f, 1.0f      // 3 top right
    )

    var mVertexArray: FloatBuffer? = null
    var mTexCoordArray: FloatBuffer? = null

    var mCoordsPerVertex: Int = -1
    var mVertexStride: Int = 0
    var mVertexCount: Int = 0
    var mTexCoordStride: Int = 0

    fun init(isRecord: Boolean) {

        mVertexArray = OpenGlUtils.createFloatBuffer(FULL_RECTANGLE_COORDS)
        if (isRecord) {
            mTexCoordArray = OpenGlUtils.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_RECORD)
        } else {
            mTexCoordArray = OpenGlUtils.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_PREVIEW)
        }

        mCoordsPerVertex = 2
        mVertexStride = mCoordsPerVertex * OpenGlUtils.SIZEOF_FLOAT
        mVertexCount = FULL_RECTANGLE_COORDS.size / mCoordsPerVertex
        mTexCoordStride = 2 * OpenGlUtils.SIZEOF_FLOAT
    }

}