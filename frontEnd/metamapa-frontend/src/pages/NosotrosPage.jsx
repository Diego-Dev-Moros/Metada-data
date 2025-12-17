import "./NosotrosPage.css";

const TEAM = [
    { nombre: "Ezequiel", rol: "CEO", foto: "/team/ezequiel.jpg" },
    { nombre: "Nara", rol: "CEO", foto: "/team/nara.jpg" },
    { nombre: "Karlene", rol: "CEO", foto: "/team/karlene.jpg" },
    { nombre: "Pedro", rol: "CEO", foto: "/team/pedro.jpg" },
    { nombre: "Candela", rol: "CEO", foto: "/team/candela.jpg" },
    { nombre: "Kiara", rol: "CEO", foto: "/team/kiara.jpg" },
];

export default function NosotrosPage() {
    return (
        <div className="nosotros-page main-content">
            <header className="nosotros-header">
                <h1>Acerca de MetaMapa</h1>
            </header>

            <section className="nosotros-section">
                <h2>Nuestra misión</h2>
                <p>
                    MetaMapa es una plataforma colaborativa diseñada para visibilizar y mapear hechos
                    relevantes en tiempo y espacio: desde incendios y conflictos ambientales, hasta
                    denuncias o eventos sociales de interés público.
                </p>
                <p>
                    Buscamos potenciar la inteligencia colectiva, permitiendo que cada persona pueda
                    contribuir con información valiosa para su comunidad.
                </p>
            </section>

            <section className="nosotros-section">
                <h2>Nuestra visión</h2>
                <p>
                    Creemos en un mundo donde la información es un bien común y la tecnología un medio
                    para fortalecer la participación ciudadana, la transparencia y la colaboración entre
                    organizaciones.
                </p>
                <p>
                    MetaMapa fue pensada para ser un software libre y abierto, adaptable a ONGs,
                    universidades o instituciones públicas que necesiten recopilar y compartir información
                    territorial de forma ética, segura y descentralizada.
                </p>
            </section>

            <section className="nosotros-equipo">
                <h2>Equipo</h2>
                <p className="equipo-sub">Nuestros integrantes</p>

                <div className="equipo-grid">
                    {TEAM.map((p) => (
                        <div key={p.nombre} className="equipo-card">
                            <div className="equipo-avatar">
                                {/* si no hay foto real, mostramos círculo gris */}
                                {p.foto ? (
                                    <img src={p.foto} alt={p.nombre} />
                                ) : (
                                    <span className="avatar-inicial">
                    {p.nombre.charAt(0)}
                  </span>
                                )}
                            </div>
                            <div className="equipo-nombre">{p.nombre}</div>
                            <div className="equipo-rol">{p.rol}</div>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
}
