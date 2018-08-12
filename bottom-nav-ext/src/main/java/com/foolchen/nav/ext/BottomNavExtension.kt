package com.foolchen.nav.ext

import android.annotation.SuppressLint
import android.support.design.internal.BaselineLayout
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.util.TypedValue
import android.widget.TextView
import java.lang.reflect.Field

@SuppressLint("RestrictedApi")
fun BottomNavigationView.disableAll() {
  disableIconTint()
  disableShiftMode(false)
  unifyItems(false)
  (getChildAt(0) as? BottomNavigationMenuView)?.updateMenuView()
}

/**
 * 禁用`app:itemIconTint`属性,让[BottomNavigationView]的图标显示原本的颜色
 */
fun BottomNavigationView.disableIconTint() {
  this.itemIconTintList = null
}

/**
 * 禁用位移动画
 */
fun BottomNavigationView.disableShiftMode(forceUpdate: Boolean = true) {
  try {
    val bottomNavigationMenuView = getChildAt(0) as BottomNavigationMenuView
    // BottomNavigationMenuView未提供setter方法来控制位移动画的开关，在不修改源码的前提下，只能通过反射来实现
    val shiftingMode = bottomNavigationMenuView.javaClass.getDeclaredField("mShiftingMode")
    shiftingMode.setBooleanValue(bottomNavigationMenuView, false)
    if (forceUpdate) {
      // 此处刷新整个BottomNavigationMenuView,防止已经显示的效果残留影响到整体效果
      // 如果不刷新的话,会导致进入时选中的Icon存在多余的顶部外边距(mShiftAmount),第一次点击时还存在位移动画
      // 该方法限制为进在com.android.support包中才能够调用,不知何时就会被隐藏掉.在被隐藏掉的时候可以尝试采用反射的方式进行调用
      bottomNavigationMenuView.updateMenuView()
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

/**
 * 统一每个条目的文本大小和图标的外边距
 */
fun BottomNavigationView.unifyItems(forceUpdate: Boolean = true) {
  try {
    val bottomNavigationMenuView = getChildAt(0) as BottomNavigationMenuView
    val childCount = bottomNavigationMenuView.childCount
    for (i in 0 until childCount) {
      val child = bottomNavigationMenuView.getChildAt(i) as BottomNavigationItemView
      val clazz = child.javaClass

      // 禁用位移动画
      val shiftingMode = clazz.getDeclaredField("mShiftingMode")
      shiftingMode.setBooleanValue(child, false)

      // 使选中/未选中条目的顶部边距不发生变化
      val shiftAmountField = clazz.getDeclaredField("mShiftAmount")
      shiftAmountField.setIntValue(child, 0)

      // 使选中/未选中条目的文本保持同样的大小
      child.unifyTextSize()
    }

    if (forceUpdate) {
      bottomNavigationMenuView.updateMenuView()
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

private fun BottomNavigationItemView.unifyTextSize() {
  val baselineLayout = getChildAt(1) as BaselineLayout
  val smallLabel = baselineLayout.getChildAt(0) as TextView
  val largeLabel = baselineLayout.getChildAt(1) as TextView
  largeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallLabel.textSize)
}

private fun Field.setBooleanValue(obj: Any, value: Boolean) {
  isAccessible = true
  setBoolean(obj, value)
  isAccessible = false
}

private fun Field.setIntValue(obj: Any, value: Int) {
  isAccessible = true
  setInt(obj, value)
  isAccessible = false
}