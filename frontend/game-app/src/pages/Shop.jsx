import { useState } from 'react'
import AppLayout from '../components/AppLayout'
import PageHeader from '../components/PageHeader'
import Notice from '../components/Notice'
import Button, { LinkButton } from '../components/Button'
import Skeleton from '../components/Skeleton'
import ErrorMessage from '../components/ErrorMessage'
import LegendaryProgress from '../components/LegendaryProgress'
import { useFetch } from '../hooks/useFetch'
import { useUser } from '../context/UserContext'
import { buy, getShop } from '../api/shop'

const count = new Intl.NumberFormat('it-IT', { useGrouping: 'always' })

/**
 * 7 / D7 · /shop
 *
 * @returns {JSX.Element}
 */
export default function Shop() {
  const { data, loading, error, reload } = useFetch(getShop)
  const { data: user, reload: reloadUser } = useUser()
  const [purchase, setPurchase] = useState({ inProgress: null, outcome: null })

  /** Buys a product and shows the outcome. */
  async function buyProduct(product) {
    setPurchase({ inProgress: product.id, outcome: null })
    try {
      await buy(product.id)
      setPurchase({ inProgress: null, outcome: { kind: 'success', text: `Hai comprato: ${product.name}. La trovi in Collezione › Buste.` } })
      reload()
      reloadUser() // the credits at the top go down
    } catch {
      setPurchase({ inProgress: null, outcome: { kind: 'error', text: 'Acquisto non riuscito. Riprova.' } })
    }
  }

  const credits = user?.credits

  return (
    <AppLayout activePage="shop" desktopBackground="/images/shop-background-desktop.png">
      <div className="flex flex-col gap-3 lg:gap-6">
        <PageHeader title="Negozio" subtitle="Spendi i crediti che guadagni giocando." />

        {loading && <Skeleton className="mt-3 h-80 rounded-2xl" />}
        {error && <ErrorMessage />}

        {data && (
          <div className="flex flex-col gap-3 pt-3 lg:flex-row lg:items-start lg:gap-6 lg:pt-0">
            {/* The credit: on mobile at the top, on desktop in the right column */}
            <div className="lg:hidden">
              <CreditInfo credits={credits} rules={data.creditRules} />
            </div>

            <div className="flex flex-col gap-3 lg:flex-1">
              {purchase.outcome && <Notice kind={purchase.outcome.kind}>{purchase.outcome.text}</Notice>}
              {data.products.map((product) => {
                const enough = credits === undefined || credits >= product.price
                return (
                  <article
                    key={product.id}
                    className="flex items-center gap-3.5 rounded-2xl border border-gold-border bg-background-secondary px-4 py-3.5 lg:gap-8 lg:border-gold lg:bg-background lg:p-6"
                  >
                    <img src={product.image} alt="" className="h-32 w-24 shrink-0 rounded-lg object-cover lg:h-90 lg:w-65" />
                    <div className="flex min-w-0 flex-1 flex-col gap-1.5 leading-[1.4] lg:gap-3">
                      <h2 className="font-semibold lg:font-title lg:text-[36px] lg:leading-normal lg:font-normal">{product.name}</h2>
                      <p className="text-caption font-medium text-ink-secondary lg:text-body lg:font-normal lg:text-ink">
                        {product.description}
                        {product.detail && <span className="hidden lg:inline"> ({product.detail}).</span>}
                      </p>
                      <p className="font-semibold text-ink-secondary lg:font-title lg:text-h2 lg:font-normal lg:text-ink">
                        {count.format(product.price)} crediti
                      </p>
                      <Button
                        onClick={() => buyProduct(product)}
                        disabled={!enough || purchase.inProgress === product.id}
                        className="w-full lg:w-auto lg:self-start lg:shadow-[0_6px_8px_rgba(0,0,0,0.15)]"
                      >
                        {!enough ? 'Crediti insufficienti' : purchase.inProgress === product.id ? 'Acquisto…' : (
                          <>
                            Compra<span className="hidden lg:inline">&nbsp;per {count.format(product.price)} crediti</span>
                          </>
                        )}
                      </Button>
                    </div>
                  </article>
                )
              })}
              <div className="lg:hidden">
                <LegendaryProgress packs={data.legendary.packs} total={data.legendary.total} />
              </div>
            </div>

            {/* 💻 Desktop: right column */}
            <aside className="hidden w-105 shrink-0 flex-col gap-4 lg:flex">
              <CreditInfo credits={credits} rules={data.creditRules} />
              <LegendaryProgress packs={data.legendary.packs} total={data.legendary.total} />
              <section className="flex flex-col gap-2.5 rounded-2xl border border-gold bg-background p-6">
                <h2 className="font-title text-h2">Le tue buste</h2>
                <p>{data.packsToOpen === 0 ? 'Non hai buste da aprire.' : `Hai ${data.packsToOpen} buste da aprire.`}</p>
                {data.packsToOpen > 0 && (
                  <LinkButton to="/collection/packs" variant="secondary" className="min-h-12 w-full">
                    Apri le buste
                  </LinkButton>
                )}
              </section>
            </aside>
          </div>
        )}
      </div>
    </AppLayout>
  )
}

/**
 * "Il tuo credito" (your credit) box with the rules to earn credits.
 *
 * @param {object} props - the component props
 * @param {number} props.credits
 * @param {string[]} props.rules
 * @returns {JSX.Element}
 */
function CreditInfo({ credits, rules }) {
  return (
    <section className="flex flex-col gap-2 rounded-2xl border border-gold-border bg-background-secondary px-4 py-3.5 lg:border-gold lg:bg-background lg:p-6">
      <div className="flex items-center justify-between lg:flex-col lg:items-start lg:gap-1.5">
        <h2 className="text-ink-secondary lg:leading-[1.4] lg:font-semibold lg:text-ink">Il tuo credito</h2>
        <p className="font-title text-[22px] lg:text-[36px]">{credits === undefined ? '…' : `${count.format(credits)} crediti`}</p>
      </div>
      <p className="text-caption leading-[1.4] font-medium text-ink-secondary lg:text-ink">{rules}</p>
    </section>
  )
}
