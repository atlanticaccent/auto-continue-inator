package com.crimes_collection

import com.crimes_collection.luna.Settings
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
import com.fs.state.AppDriver
import com.fs.state.AppState
import kotlin.math.roundToInt

@Suppress("unused")
class MenuPlugin : BaseEveryFrameCombatPlugin() {
    companion object {
        private var _complete = false
        var complete: Boolean
            get() {
                return if (Global.getSettings().modManager.isModEnabled("lunalib") && Settings.trapMod) {
                    false
                } else {
                    _complete
                }
            }
            set(value) {
                _complete = value
            }
    }

    private lateinit var engine: CombatEngineAPI
    private val state: AppState?
        get() = AppDriver.getInstance().currentState
    private var flip = false
    private var doHack = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun init(engine: CombatEngineAPI) {
        this.engine = engine
    }

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>) {
        if (complete) return
        (state as? TitleScreenState)?.let { titleState ->
            val screenPanel = ReflectionUtils.invoke("getScreenPanel", titleState) as UIPanelAPI
            val buttonHolder = screenPanel.getChildrenCopy()[0].getChildrenCopy()[0]
            val curr = ReflectionUtils.invoke("getCurr", buttonHolder) as UIPanelAPI
            val continueButton =
                curr.getChildrenCopy()[0].getChildrenCopy().firstOrNull { (it as ButtonAPI).text == "Continue" }
            if (continueButton != null) {
                val pos = ReflectionUtils.invoke("getPosition", continueButton) as PositionAPI
                val listType = ReflectionUtils.getMethodParameterTypes(continueButton, "processInputImpl")!![0]!!
                val superclass = ReflectionUtils.invoke("getGenericSuperclass", listType)!!
                val eventType = (ReflectionUtils.invoke("getActualTypeArguments", superclass)!! as Array<*>)[0]
                val event = ReflectionUtils.instantiate(
                    eventType as Class<*>, InputEventClass.MOUSE_EVENT, if (flip) {
                        InputEventType.MOUSE_UP
                    } else {
                        InputEventType.MOUSE_DOWN
                    }, pos.x.roundToInt(), pos.y.roundToInt(), 0, '\u0000'
                )
                continueButton.processInput(listOf(event as InputEventAPI))
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