import TradeCard from './TradeCard'

/**
 * The two cards of a trade, with the symbol in the middle (⇄ or ✓ once the trade is done)
 *
 * @param {object} props - the component props
 * @param {object} props.left
 * @param {object} props.right
 * @param {string} [props.symbol='⇄']
 * @param {object} [props.options] - all the other props
 * @returns {JSX.Element}
 */
export default function CardPair({ left, right, symbol = '⇄', ...options }) {
  return (
    <div className="flex items-center justify-between">
      <TradeCard {...left} {...options} />
      <span aria-hidden="true" className={`font-title text-[22px] ${symbol === '✓' ? 'text-[40px] text-success' : 'text-ink-secondary'}`}>
        {symbol}
      </span>
      <TradeCard {...right} {...options} />
    </div>
  )
}
