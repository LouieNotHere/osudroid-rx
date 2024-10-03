package com.reco1l.osu.playfield

import com.edlplan.framework.easing.*
import com.reco1l.andengine.*
import com.reco1l.andengine.Anchor
import com.reco1l.andengine.container.*
import com.reco1l.andengine.sprite.*
import com.reco1l.andengine.texture.*
import com.reco1l.framework.*
import org.anddev.andengine.engine.camera.*
import org.anddev.andengine.opengl.texture.region.*
import org.anddev.andengine.opengl.util.*
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer
import ru.nsu.ccfit.zuev.osu.*
import ru.nsu.ccfit.zuev.osu.scoring.*
import ru.nsu.ccfit.zuev.skins.*
import javax.microedition.khronos.opengles.*


class HealthDisplay(private val statistics: StatisticV2) : Container() {


    private val fill: AnimatedSpriteWithDepthTest

    private val fillClear: DepthClearRectangle

    private val marker: ExtendedSprite

    private val explode: ExtendedSprite

    private val kiTextures: Array<TextureRegion>?

    private val isNewStyle: Boolean


    private var lastHP = 0f


    init {

        val backgroundTexture = ResourceManager.getInstance().getTexture("scorebar-bg")
        val markerTexture = ResourceManager.getInstance().getTextureIfLoaded("scorebar-marker")

        // the marker lookup to decide which display style must be performed on the source of the bg, which is the most common element.
        isNewStyle = backgroundTexture !is BlankTextureRegion && markerTexture != null

        // background implementation is the same for both versions.
        attachChild(ExtendedSprite().apply { textureRegion = backgroundTexture })

        fillClear = DepthClearRectangle()
        fillClear.setOrigin(Anchor.TopRight)
        attachChild(fillClear)

        fill = AnimatedSpriteWithDepthTest("scorebar-colour", true, OsuSkin.get().animationFramerate)
        fill.autoSizeAxes = Axes.None // Preserve the first frame width.
        attachChild(fill)

        marker = ExtendedSprite()
        marker.setOrigin(Anchor.Center)
        attachChild(marker)

        explode = ExtendedSprite()
        explode.setOrigin(Anchor.Center)
        explode.blendingFunction = BlendingFunction.Additive
        explode.alpha = 0f
        attachChild(explode)

        if (isNewStyle) {
            fill.setPosition(7.5f * 1.6f, 7.8f * 1.6f)

            marker.textureRegion = markerTexture
            explode.textureRegion = markerTexture

            kiTextures = null
        } else {
            fill.setPosition(3f * 1.6f, 10f * 1.6f)

            kiTextures = arrayOf(
                ResourceManager.getInstance().getTextureIfLoaded("scorebar-ki"),
                ResourceManager.getInstance().getTextureIfLoaded("scorebar-kidanger"),
                ResourceManager.getInstance().getTextureIfLoaded("scorebar-kidanger2")
            )

            marker.textureRegion = kiTextures[0]
            explode.textureRegion = kiTextures[0]
        }

        fillClear.width = 0f
        fillClear.height = fill.height
        fillClear.setPosition(fill.x + fill.width, fill.y)
    }


    override fun onManagedUpdate(pSecondsElapsed: Float) {

        fillClear.width = Interpolation.floatAt(pSecondsElapsed.coerceIn(0f, 0.2f), fillClear.width, (1f - statistics.hp) * fill.width, 0f, 0.2f, Easing.OutQuint)

        marker.x = fill.x + fill.width - fillClear.width
        marker.y = fill.y + (if (isNewStyle) fill.height / 2 else 0f)

        explode.setPosition(marker)

        if (statistics.hp > lastHP) {
            bulge()
        }

        lastHP = statistics.hp

        if (isNewStyle) {

            val color = getFillColor(statistics.hp)

            fill.color = color
            marker.color = color
            marker.blendingFunction = if (statistics.hp < EPIC_CUTOFF) BlendingFunction.Inherit else BlendingFunction.Additive

        } else {

            marker.textureRegion = kiTextures!![when {
                statistics.hp < 0.2f -> 2
                statistics.hp < EPIC_CUTOFF -> 1
                else -> 0
            }]

        }

        super.onManagedUpdate(pSecondsElapsed)
    }


    fun flash() {

        val isEpic = statistics.hp >= EPIC_CUTOFF

        bulge()

        explode.clearEntityModifiers()
        explode.blendingFunction = if (isEpic) BlendingFunction.Additive else BlendingFunction.Inherit
        explode.alpha = 1f
        explode.setScale(1f)

        explode.scaleTo(if (isEpic) 2f else 1.6f, 0.12f)
        explode.fadeOut(0.12f)
    }

    private fun bulge() {
        marker.clearEntityModifiers()
        marker.setScale(1.4f)
        marker.scaleTo(1f, 0.2f).eased(Easing.Out)
    }

    private fun getFillColor(hp: Float) = when {

        hp < 0.2f -> Colors.interpolateNonLinear(0.2f - hp, ColorARGB.Black, ColorARGB.Red, 0f, 0.2f)
        hp < EPIC_CUTOFF -> Colors.interpolateNonLinear(0.5f - hp, ColorARGB.White, ColorARGB.Black, 0f, 0.5f)

        else -> ColorARGB.White
    }


    private inner class DepthClearRectangle : ExtendedEntity(vertexBuffer = RectangleVertexBuffer(GL11.GL_STATIC_DRAW, true)) {

        init {
            color = ColorARGB.Transparent
        }

        override fun onInitDraw(pGL: GL10) {
            super.onInitDraw(pGL)
            pGL.glClear(GL10.GL_DEPTH_BUFFER_BIT)
        }

        override fun drawVertices(pGL: GL10, pCamera: Camera) {
            GLHelper.enableDepthTest(pGL)
            super.drawVertices(pGL, pCamera)
            GLHelper.disableDepthTest(pGL)
        }

        override fun onUpdateVertexBuffer() {
            (vertexBuffer as RectangleVertexBuffer).update(width, height)
        }

    }

    private inner class AnimatedSpriteWithDepthTest(textureName: String, withHyphen: Boolean, fps: Float) : AnimatedSprite(textureName, withHyphen, fps) {

        override fun drawVertices(pGL: GL10, pCamera: Camera) {
            GLHelper.enableDepthTest(pGL)
            super.drawVertices(pGL, pCamera)
            GLHelper.disableDepthTest(pGL)
        }

    }


    companion object {

        const val EPIC_CUTOFF = 0.5f

    }

}