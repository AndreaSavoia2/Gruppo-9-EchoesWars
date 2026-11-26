// The 5 menu entries: we write them ONCE only
// and they are used both by the side bar (desktop) and by the bottom menu (mobile).
// "path" = the address of the page (e.g. /collection)
export const menuItems = [
  { id: 'play', label: 'Gioca', icon: '/icons/play.png', path: '/' },
  { id: 'collection', label: 'Collezione', icon: '/icons/collection.svg', path: '/collection' },
  { id: 'shop', label: 'Negozio', icon: '/icons/shop.svg', path: '/shop' },
  { id: 'trades', label: 'Scambi', icon: '/icons/trade.svg', path: '/trades' },
  { id: 'friends', label: 'Amici', icon: '/icons/friends.svg', path: '/friends' },
]

// On mobile "Gioca" (play) is in the middle: here we only write the order.
export const mobileOrder = ['shop', 'collection', 'play', 'trades', 'friends']
