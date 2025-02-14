package xyz.wagyourtail.konig.editor.gui.window.canvas

interface CanvasComponent {

    val canvas: Canvas<*>

    fun process()

    var active: Boolean
        get() = this in canvas.activeComponents
        set(value) {
            if (value) {
                canvas.activeComponents.add(this)
            } else if (canvas.activeComponents == this) {
                canvas.activeComponents.remove(this)
            }
        }

}