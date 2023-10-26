package com.crimes_collection

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.input.InputEventClass
import com.fs.starfarer.api.input.InputEventType
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.title.TitleScreenState
import com.fs.starfarer.util.A.Object
import com.fs.state.AppDriver
import com.fs.state.AppState
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.math.roundToInt

@Suppress("unused")
class MenuPlugin : BaseEveryFrameCombatPlugin() {
    private lateinit var engine: CombatEngineAPI
    private val state: AppState?
        get() = AppDriver.getInstance().currentState
    private var flip = false
    private var doHack = true
    private var complete = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun init(engine: CombatEngineAPI) {
        this.engine = engine
    }

    private fun getScreenPanel() : UIPanelAPI {
        val title = state as TitleScreenState

        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null
        for (method in title::class.java.methods as Array<*>) {
            if (getNameMethod.invoke(method) == "getScreenPanel") {
                foundMethod = method
            }
        }

        return invokeMethod.invoke(foundMethod, title) as UIPanelAPI
    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>) {
        if (complete) return
        (state as? TitleScreenState)?.let {
            val screenPanel = getScreenPanel()
            val buttonHolder = screenPanel.getChildrenCopy()[0].getChildrenCopy()[0]
            val curr = ReflectionUtils.invoke("getCurr", buttonHolder) as UIPanelAPI
            val continueButton =
                curr.getChildrenCopy()[0].getChildrenCopy().firstOrNull { (it as ButtonAPI).text == "Continue" }
            if (continueButton != null) {
                val pos = ReflectionUtils.invoke("getPosition", continueButton) as PositionAPI
                if (flip) {
                    val event = Object(InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_UP, pos.x.roundToInt(), pos.y.roundToInt(), 0, '\u0000')
                    continueButton.processInput(listOf(event))
                } else {
                    val event = Object(InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_DOWN, pos.x.roundToInt(), pos.y.roundToInt(), 0, '\u0000')
                    continueButton.processInput(listOf(event))
                }
                flip = !flip
            } else {
                doHack = false
            }
        }
        // Horrible horrible hack to fix console commands nulling out on combat engine
        if (Global.getCombatEngine() == null && ::engine.isInitialized && doHack) {
            Global.setCombatEngine(engine)
            doHack = false
            complete = true
        }
    }
}