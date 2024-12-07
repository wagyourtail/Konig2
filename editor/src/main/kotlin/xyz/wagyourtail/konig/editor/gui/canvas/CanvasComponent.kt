package xyz.wagyourtail.konig.editor.gui.canvas

interface CanvasComponent {

    val canvas: Canvas<*>

    fun process()

    var active: Boolean
        get() = this in canvas.activeComponent
        set(value) {
            if (value) {
                canvas.activeComponent.add(this)
            } else if (canvas.activeComponent == this) {
                canvas.activeComponent.remove(this)
            }
        }

}