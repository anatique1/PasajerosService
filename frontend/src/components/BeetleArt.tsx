export function BeetleLogo() {
  return <svg viewBox="0 0 130 62" className="beetle-logo" fill="none" aria-hidden="true">
    <path d="M8 42Q12 36 24 33L37 31Q45 22 56 22L69 32L84 32Q99 34 109 41L117 44Q120 48 115 49H105Q103 40 94 40Q84 40 82 50H49Q47 40 38 40Q28 40 26 50H14Q7 49 8 42Z" fill="#E23B2E"/>
    <path d="M52 31V24Q53 19 58 19H63Q68 20 69 25V32Z" fill="#64443A"/>
    <path d="M71 24L75 24L84 33H77Z" fill="#B9D6DA" stroke="#FFF1DF" strokeWidth="1.5"/>
    <circle cx="38" cy="49" r="6" fill="#382E2A"/><circle cx="38" cy="49" r="3" fill="#EADFCF"/>
    <circle cx="94" cy="49" r="6" fill="#382E2A"/><circle cx="94" cy="49" r="3" fill="#EADFCF"/>
    <circle cx="116" cy="43" r="2.3" fill="#FFF3D6"/>
  </svg>
}

export function BeetleScene() {
  return <div className="hero-art" aria-hidden="true">
    <object className="landscape-art" data="/roadtrip-scene.svg" type="image/svg+xml" aria-label="Un convertible sube por una carretera entre colinas" />
  </div>
}
