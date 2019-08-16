package im.toss.test.specdocs

import kotlin.reflect.KClass

/**
 * 매우 제한된 조건하에서만 동작한다.
 *
 * 1. parameterized test
 * 2. parameterized test의 parameter는 SpecParameter 의 구현 단 1개
 *
 * 쓸만해지려면 이보다 훨씬 쓰기 쉬워야한다. 동작하는 조건을 놓치기 어렵게 만들어야한다.
 */
annotation class SpecGeneration(
    val value: KClass<out SpecDescriptor>
)
