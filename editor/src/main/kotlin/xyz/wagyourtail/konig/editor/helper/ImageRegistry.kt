package xyz.wagyourtail.konig.editor.helper

import org.lwjgl.opengl.GL11
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.konig.ResourceLoader
import xyz.wagyourtail.konig.structure.Image
import java.nio.IntBuffer
import javax.imageio.ImageIO

object ImageRegistry {

    private val ids = defaultedMapOf<Image, Int?> {
        ResourceLoader.resolveSrc(it.src)?.use { src ->
            val image = ImageIO.read(src.inputStream())

            val texId = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0)
            val pixels = IntBuffer.wrap(image.getRGB(0, 0, image.width, image.height, null, 0, image.width))
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.width, image.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels)

            texId
        }
    }

    operator fun get(image: Image): Int? {
        return ids[image]
    }

    fun clear() {
        ids.clear()
    }

}