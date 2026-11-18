import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { ArrowDownUp, ArrowRight, CalendarDays, Check, ChevronDown, MapPin, Search, ShieldCheck, UsersRound } from 'lucide-react'
import { BeetleLogo, BeetleScene } from './components/BeetleArt'

type Viaje = {
  id: string
  origen: string
  destino: string
  paradas?: string[]
  fechaHoraSalida: string
  cuposDisponibles: number
  aportePorCupo?: number | string
  conductorId?: string
  estado?: string
}
type Usuario = { id: string; correo: string; nombre: string; fotoUrl?: string; rol: string }
type Reserva = {
  id: string
  viajeId: string
  origen: string
  destino: string
  fechaHoraSalida?: string
  aportePorCupo?: number | string
  puntoRecogida: string
  estado: 'CONFIRMADA' | 'CANCELADA'
  creadaEn: string
}

const apiPasajeros = import.meta.env.VITE_PASAJEROS_API_URL || 'http://localhost:8082/api'
const apiViajes = import.meta.env.VITE_RUTAMACHT_API_URL || 'http://localhost:8081/api'
const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

async function leerRespuesta(response: Response) {
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.mensaje || data.detail || data.message || `Error ${response.status} al procesar la solicitud.`)
  return data
}

function App() {
  const buttonRef = useRef<HTMLDivElement>(null)
  const googleInitialized = useRef(false)
  const googleButtonRendered = useRef(false)
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [googleCredential, setGoogleCredential] = useState<string | null>(null)
  const [vista, setVista] = useState<'explorar' | 'pasajero'>('explorar')
  const [viajes, setViajes] = useState<Viaje[]>([])
  const [reservas, setReservas] = useState<Reserva[]>([])
  const [viajeSeleccionado, setViajeSeleccionado] = useState<Viaje | null>(null)
  const [puntoRecogida, setPuntoRecogida] = useState('')
  const [origen, setOrigen] = useState('')
  const [destino, setDestino] = useState('')
  const [cargando, setCargando] = useState(false)
  const [cargandoLogin, setCargandoLogin] = useState(false)
  const [cargandoReservas, setCargandoReservas] = useState(false)
  const [guardandoReserva, setGuardandoReserva] = useState(false)
  const [cancelandoReserva, setCancelandoReserva] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [authError, setAuthError] = useState('')
  const [reservaError, setReservaError] = useState('')
  const [avisoReserva, setAvisoReserva] = useState('')
  const [buscado, setBuscado] = useState(false)

  const buscarViajes = useCallback(async (event?: FormEvent) => {
    event?.preventDefault()
    setCargando(true)
    setError('')
    setBuscado(true)
    const query = new URLSearchParams()
    if (origen.trim()) query.set('origen', origen.trim())
    if (destino.trim()) query.set('destino', destino.trim())
    try {
      const response = await fetch(`${apiViajes}/viajes?${query.toString()}`)
      if (!response.ok) throw new Error('No pudimos consultar los viajes. Revisa que el servicio esté activo.')
      const data = await response.json()
      setViajes(Array.isArray(data) ? data : (data.viajes ?? []))
    } catch (e) {
      setViajes([])
      setError(e instanceof Error ? e.message : 'No se pudo conectar con RutaMacht.')
    } finally { setCargando(false) }
  }, [origen, destino])

  const cargarReservas = useCallback(async (credential: string) => {
    setCargandoReservas(true)
    setReservaError('')
    try {
      const response = await fetch(`${apiViajes}/reservas/mis`, { headers: { Authorization: `Bearer ${credential}` } })
      const data = await leerRespuesta(response)
      setReservas(data.reservas ?? [])
    } catch (e) {
      setReservaError(e instanceof Error ? e.message : 'No se pudieron cargar tus reservas.')
    } finally { setCargandoReservas(false) }
  }, [])

  useEffect(() => {
    if (googleCredential) void cargarReservas(googleCredential)
    else setReservas([])
  }, [googleCredential, cargarReservas])

  useEffect(() => {
    if (!clientId || !buttonRef.current) return
    const render = () => {
      if (!window.google || !buttonRef.current) return false
      if (!googleInitialized.current) {
        window.google.accounts.id.initialize({ client_id: clientId, callback: async ({ credential }) => {
          setCargandoLogin(true)
          setAuthError('')
          try {
            const response = await fetch(`${apiPasajeros}/auth/google`, {
              method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ credential }),
            })
            const data = await leerRespuesta(response)
            setUsuario(data.usuario)
            setGoogleCredential(credential)
            setVista('pasajero')
            window.location.hash = 'panel'
          } catch (e) {
            setAuthError(e instanceof Error ? e.message : 'No se pudo iniciar sesión.')
          } finally { setCargandoLogin(false) }
        } })
        googleInitialized.current = true
      }
      if (!googleButtonRendered.current) {
        window.google.accounts.id.renderButton(buttonRef.current, { theme: 'outline', size: 'large', shape: 'pill', text: 'signin_with', locale: 'es', width: 260 })
        googleButtonRendered.current = true
      }
      return true
    }
    if (render()) return
    const timer = window.setInterval(() => { if (render()) window.clearInterval(timer) }, 200)
    return () => window.clearInterval(timer)
  }, [])

  const cerrarSesion = () => {
    window.google?.accounts.id.disableAutoSelect()
    setUsuario(null)
    setGoogleCredential(null)
    setVista('explorar')
  }
  const abrirReserva = (viaje: Viaje) => {
    setReservaError('')
    setAvisoReserva('')
    if (!googleCredential) {
      setAuthError('Inicia sesión con Google para reservar un cupo.')
      window.scrollTo({ top: 0, behavior: 'smooth' })
      return
    }
    setPuntoRecogida('')
    setViajeSeleccionado(viaje)
  }
  const confirmarReserva = async (event: FormEvent) => {
    event.preventDefault()
    if (!googleCredential || !viajeSeleccionado) return
    setGuardandoReserva(true)
    setReservaError('')
    try {
      const response = await fetch(`${apiViajes}/viajes/${viajeSeleccionado.id}/reservas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${googleCredential}` },
        body: JSON.stringify({ puntoRecogida }),
      })
      const data = await leerRespuesta(response)
      setViajeSeleccionado(null)
      setAvisoReserva(data.mensaje || 'Cupo reservado correctamente.')
      await Promise.all([cargarReservas(googleCredential), buscarViajes()])
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (e) {
      setReservaError(e instanceof Error ? e.message : 'No se pudo completar la reserva.')
    } finally { setGuardandoReserva(false) }
  }
  const cancelarReserva = async (reserva: Reserva) => {
    if (!googleCredential || !window.confirm(`¿Quieres cancelar tu cupo para ${reserva.origen} → ${reserva.destino}?`)) return
    setCancelandoReserva(reserva.id)
    setReservaError('')
    try {
      const response = await fetch(`${apiViajes}/reservas/${reserva.id}`, {
        method: 'DELETE', headers: { Authorization: `Bearer ${googleCredential}` },
      })
      const data = await leerRespuesta(response)
      setAvisoReserva(data.mensaje || 'Reserva cancelada.')
      await Promise.all([cargarReservas(googleCredential), buscarViajes()])
    } catch (e) {
      setReservaError(e instanceof Error ? e.message : 'No se pudo cancelar la reserva.')
    } finally { setCancelandoReserva(null) }
  }
  const intercambiar = () => { setOrigen(destino); setDestino(origen) }
  const fecha = (value: string) => new Intl.DateTimeFormat('es-CO', { weekday: 'short', day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' }).format(new Date(value))
  const moneda = (value?: number | string) => value == null ? 'A convenir' : new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(value))

  return <div className="app-shell">
    <header className="topbar">
      <a className="brand" href="#inicio" aria-label="RutaMacht inicio"><span className="brand-mark"><BeetleLogo /></span><span>Ruta<span className="brand-accent">Macht</span></span></a>
      <nav>{usuario ? <><a className="nav-active" href="#panel">Mi espacio</a><a href="#buscar" onClick={() => setVista('explorar')}>Buscar viajes</a></> : <><a className="nav-active" href="#buscar">Buscar viajes</a><a href="#como-funciona">Cómo funciona</a></>}</nav>
      <div className="account-area">
        {usuario ? <div className="profile"><img src={usuario.fotoUrl || 'https://placehold.co/40x40?text=R'} alt="" /><span className="profile-name">{usuario.nombre}</span><button className="logout" onClick={cerrarSesion}>Salir</button></div>
          : <div className="google-wrap"><div ref={buttonRef} />{!clientId && <span className="setup-note">Configura VITE_GOOGLE_CLIENT_ID</span>}</div>}
      </div>
    </header>

    <main id="inicio">
      {usuario && vista === 'pasajero' ? <section className="passenger-dashboard" id="panel">
        <div className="dashboard-welcome"><span className="section-kicker">TU ESPACIO EN RUTAMACHT</span><div className="welcome-row"><div><h1>¡Hola, {usuario.nombre.split(' ')[0]}!</h1><p>Administra tus cupos y encuentra nuevos viajes compartidos.</p></div><img src={usuario.fotoUrl || 'https://placehold.co/72x72?text=R'} alt="" /></div><button className="search-button dashboard-search" onClick={() => { setVista('explorar'); window.location.hash = 'buscar' }}><Search size={18}/> Buscar un viaje <ArrowRight size={17}/></button></div>
        <div className="dashboard-cards"><article className="dashboard-card account-card"><span className="dashboard-card-icon"><ShieldCheck size={20}/></span><span className="section-kicker">TU CUENTA</span><h2>{usuario.nombre}</h2><p>{usuario.correo}</p><span className="account-badge"><Check size={14}/> Sesión iniciada con Google</span></article>
          <article className="dashboard-card reservation-summary"><span className="dashboard-card-icon"><CalendarDays size={20}/></span><span className="section-kicker">TUS RESERVAS</span><h2>{reservas.filter(r => r.estado === 'CONFIRMADA').length} activas</h2><p>Las reservas quedan asociadas a tu cuenta de Google.</p></article></div>
        <div className="my-reservations"><div className="reservations-heading"><div><span className="section-kicker">TUS VIAJES</span><h2>Mis reservas</h2></div><button className="sort-button" onClick={() => googleCredential && void cargarReservas(googleCredential)}>Actualizar <ArrowRight size={15}/></button></div>
          {cargandoReservas && <div className="empty-state"><span className="loader"/> Cargando tus reservas…</div>}
          {!cargandoReservas && reservaError && <div className="error-state">{reservaError}</div>}
          {!cargandoReservas && !reservaError && reservas.length === 0 && <div className="empty-state"><div className="empty-icon"><CalendarDays size={20}/></div><strong>Aún no tienes reservas</strong><span>Explora los viajes y reserva un cupo para verlo aquí.</span><button className="inline-link" onClick={() => { setVista('explorar'); window.location.hash = 'buscar' }}>Buscar viajes <ArrowRight size={15}/></button></div>}
          {!cargandoReservas && reservas.length > 0 && <div className="reservation-list">{reservas.map(reserva => <article className="reservation-card" key={reserva.id}><div className="reservation-route"><span className={`reservation-state ${reserva.estado === 'CONFIRMADA' ? 'confirmed' : 'cancelled'}`}>{reserva.estado === 'CONFIRMADA' ? 'Confirmada' : 'Cancelada'}</span><strong>{reserva.origen} <ArrowRight size={15}/> {reserva.destino}</strong><span className="reservation-date">{reserva.fechaHoraSalida ? fecha(reserva.fechaHoraSalida) : 'Fecha no disponible'} · Recogida: {reserva.puntoRecogida}</span></div><div className="reservation-actions"><strong>{moneda(reserva.aportePorCupo)}</strong>{reserva.estado === 'CONFIRMADA' && <button className="cancel-button" disabled={cancelandoReserva === reserva.id} onClick={() => void cancelarReserva(reserva)}>{cancelandoReserva === reserva.id ? 'Cancelando…' : 'Cancelar cupo'}</button>}</div></article>)}</div>}
        </div>
      </section> : <>
      <section className="hero">
        <div className="hero-copy"><div className="eyebrow"><span className="eyebrow-dot" /> MENOS ASIENTOS VACÍOS, MÁS CAMINOS COMPARTIDOS</div>
          <h1>Tu camino va<br />mejor <em>acompañado.</em></h1>
          <p>Encuentra a alguien que va para el mismo lado. Comparte el viaje, divide el gasto y llega con una buena historia.</p>
          <div className="hero-proof"><span><ShieldCheck size={16} /> Perfiles verificados</span><i /> <span><UsersRound size={16} /> Comunidad cercana</span></div>
        </div>
        <BeetleScene />
      </section>

      <section className="search-section" id="buscar">
        <div className="search-heading"><div><span className="section-kicker">ENCUENTRA TU RUTA</span><h2>¿Para dónde vas?</h2></div><span className="open-tag"><span/> Viajes abiertos</span></div>
        <form className="search-card" onSubmit={buscarViajes}>
          <label className="field"><span>ORIGEN</span><div><MapPin size={18}/><input value={origen} onChange={e => setOrigen(e.target.value)} placeholder="¿Desde dónde sales?" /></div></label>
          <button className="swap" type="button" onClick={intercambiar} aria-label="Intercambiar origen y destino"><ArrowDownUp size={17}/></button>
          <label className="field"><span>DESTINO</span><div><MapPin size={18}/><input value={destino} onChange={e => setDestino(e.target.value)} placeholder="¿A dónde quieres llegar?" /></div></label>
          <button className="search-button" type="submit"><Search size={18}/><span>Buscar viajes</span><ArrowRight size={17}/></button>
        </form>
        <div className="search-foot"><span>Prueba con ciudades, barrios o lugares de referencia</span><span className="date-hint"><CalendarDays size={15}/> Próximamente: filtro por fecha</span></div>
      </section>

      <section className="results-section">
        <div className="results-heading"><div><span className="section-kicker">RUTAS QUE TE PUEDEN SERVIR</span><h2>{buscado ? 'Viajes encontrados' : 'Explora próximos viajes'}</h2></div><button className="sort-button" onClick={() => setViajes(current => [...current].sort((a,b) => new Date(a.fechaHoraSalida).getTime() - new Date(b.fechaHoraSalida).getTime()))}>Próximos primero <ChevronDown size={16}/></button></div>
        {cargando && <div className="empty-state"><span className="loader"/> Buscando viajes disponibles…</div>}
        {!cargando && error && <div className="error-state">{error}</div>}
        {!cargando && !error && viajes.length > 0 && <div className="trip-grid">{viajes.map(viaje => <article className="trip-card" key={viaje.id}>
          <div className="trip-card-top"><span className="trip-status"><i/> Salida programada</span><span className="trip-date"><CalendarDays size={15}/> {fecha(viaje.fechaHoraSalida)}</span></div>
          <div className="route"><div className="route-line"><span/><i/><span/></div><div className="route-cities"><strong>{viaje.origen}</strong><span>{viaje.paradas?.length ? `${viaje.paradas.length} parada${viaje.paradas.length > 1 ? 's' : ''}` : 'Directo'}</span><strong>{viaje.destino}</strong></div></div>
          <div className="trip-card-bottom"><div className="trip-detail"><UsersRound size={17}/><span>{viaje.cuposDisponibles} {viaje.cuposDisponibles === 1 ? 'cupo' : 'cupos'}</span></div><div className="trip-price"><small>APORTE POR CUPO</small><strong>{moneda(viaje.aportePorCupo)}</strong></div></div>
          <button className="trip-cta" disabled={viaje.cuposDisponibles < 1} onClick={() => abrirReserva(viaje)}>{viaje.cuposDisponibles < 1 ? 'Sin cupos' : 'Reservar cupo'} <ArrowRight size={16}/></button>
        </article>)}</div>}
        {!cargando && !error && viajes.length === 0 && <div className="empty-state"><div className="empty-icon"><MapPin size={21}/></div><strong>{buscado ? 'Todavía no hay viajes para esa ruta' : 'Tu próximo viaje puede empezar aquí'}</strong><span>{buscado ? 'Prueba otras ciudades o cambia el origen y el destino.' : 'Busca una ruta para ver quién va hacia el mismo destino.'}</span></div>}
      </section>

      <section className="how-section" id="como-funciona"><div><span className="section-kicker">SENCILLO, COMO DEBE SER</span><h2>Compartir el camino<br/>nos acerca.</h2></div><div className="steps"><div className="step"><span>01</span><div><strong>Encuentra tu ruta</strong><p>Busca viajes que coincidan con tu recorrido.</p></div></div><div className="step"><span>02</span><div><strong>Conoce el trayecto</strong><p>Revisa horarios, paradas y cupos disponibles.</p></div></div><div className="step"><span>03</span><div><strong>Viaja acompañado</strong><p>Reserva tu lugar y comparte el costo del viaje.</p></div></div></div></section>
      </>}
      <footer><a className="brand footer-brand" href="#inicio"><span className="brand-mark"><BeetleLogo /></span><span>Ruta<span className="brand-accent">Macht</span></span></a><span>Compartimos el camino. · Colombia</span><span className="footer-check"><Check size={14}/> Hecho para conectar</span></footer>
    </main>
    {viajeSeleccionado && <div className="modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget && !guardandoReserva) setViajeSeleccionado(null) }}>
      <form className="booking-modal" onSubmit={confirmarReserva}>
        <button type="button" className="modal-close" onClick={() => setViajeSeleccionado(null)} aria-label="Cerrar">×</button>
        <span className="section-kicker">CONFIRMA TU CUPO</span><h2>{viajeSeleccionado.origen} <ArrowRight size={19}/> {viajeSeleccionado.destino}</h2>
        <p className="modal-trip-date">{fecha(viajeSeleccionado.fechaHoraSalida)} · {moneda(viajeSeleccionado.aportePorCupo)} por cupo</p>
        <label className="pickup-field">Punto de recogida<input autoFocus required maxLength={160} value={puntoRecogida} onChange={e => setPuntoRecogida(e.target.value)} placeholder="Ej. Parque principal, entrada norte" /></label>
        {reservaError && <div className="error-state modal-error">{reservaError}</div>}
        <button className="search-button confirm-booking" type="submit" disabled={guardandoReserva}>{guardandoReserva ? 'Reservando…' : 'Confirmar reserva'} <ArrowRight size={17}/></button>
      </form>
    </div>}
    {cargandoLogin && <div className="toast"><span className="loader"/> Verificando tu cuenta con Google…</div>}
    {authError && !cargandoLogin && <div className="toast auth-error-toast" role="alert">{authError}</div>}
    {avisoReserva && !viajeSeleccionado && <div className="toast reservation-toast" role="status">{avisoReserva}<button onClick={() => setAvisoReserva('')} aria-label="Cerrar">×</button></div>}
  </div>
}

export default App
