import groovy.lang.Closure

package object awsplugin {
  implicit def function2Closure(fn: () => Unit): Closure[_ <: Unit] = new Closure((): Unit) { fn() }
}
