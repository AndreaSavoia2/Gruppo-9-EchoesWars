import { useEffect, useState } from 'react'

/**
 * Makes a number drop one step at a time, from "from" to "to" (e.g. DEF 4 → 3 → 2 → 1).
 * If "to" is bigger than "from", or the user asked for "reduce motion",
 * it shows the final number straight away.
 *
 * Usage: const { value, counting } = useCountDown(4, 1, 1)
 *   value    → the number to show now
 *   counting → true while it is still dropping (to colour it red)
 *
 * @param {number} from - starting number
 * @param {number} to - final number (below 0 it stops at 0)
 * @param {number} [speed=1] - 1 normal, 2 double speed
 * @returns {{ value: number, counting: boolean }}
 */
export function useCountDown(from, to, speed = 1) {
  const target = Math.max(to, 0)
  const [value, setValue] = useState(from)

  useEffect(() => {
    const reduced = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    if (reduced || from <= target) {
      setValue(target)
      return
    }
    setValue(from)
    let current = from
    const timer = setInterval(() => {
      current -= 1
      setValue(current)
      if (current <= target) clearInterval(timer)
    }, 170 / speed)
    return () => clearInterval(timer)
  }, [from, target, speed])

  return { value, counting: value > target }
}
