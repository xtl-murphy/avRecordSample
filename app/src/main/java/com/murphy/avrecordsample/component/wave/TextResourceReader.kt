package com.murphy.opengldemo.feature.component.wave

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

class TextResourceReader {

    companion object {
        fun readTextFileFromResource(context: Context, resourceId: Int): String {
            val body = StringBuilder()

            try {
                val inputStream = context.resources.openRawResource(resourceId)
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var nextLine: String? = null

                while ({ nextLine = bufferedReader.readLine();nextLine }() != null) {
                    body.append(nextLine)
                    body.append('\n')
                }
            } catch (e: IOException) {
                throw RuntimeException("Could not open resource: " + resourceId, e)
            } catch (nfe: Resources.NotFoundException) {
                throw RuntimeException("Resource not found: " + resourceId, nfe)
            }

            return body.toString()
        }
    }
}