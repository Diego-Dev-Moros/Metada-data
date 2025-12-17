import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import "./NuevaSolicitudEliminacionPage.css";

export default function NuevaSolicitudEliminacionPage() {
    const [motivo, setMotivo] = useState("");
    const navigate = useNavigate();
    const location = useLocation();

    // si venís de /coleccion con un estado {hechoId: ...}
    const { hechoId, tituloHecho} = location.state || {};

    const handleSubmit = (e) => {
        e.preventDefault();

        // validación simple
        if (motivo.trim().length < 200) {
            alert("El motivo debe tener al menos 200 caracteres.");
            return;
        }

        const payload = {
            hechoId: hechoId,           // puede ser null si no lo mandaste
            descripcion: motivo.trim(),
        };

        console.log("Solicitud de eliminación enviada:", payload);

        // TODO: POST a tu backend:
        // fetch("http://localhost:8081/solicitudes-eliminacion", {
        //   method: "POST",
        //   headers: { "Content-Type": "application/json" },
        //   body: JSON.stringify(payload),
        // })

        alert("Solicitud enviada ✔️");
        navigate("/"); // o volver a coleccion
    };

    return (
        <div className="solicitud-page">
            <div className="solicitud-card">
                <h1>Reportar Hecho</h1>
                <p>Reportando: <strong>{tituloHecho || "Hecho seleccionado"}</strong> (ID: {hechoId})</p>
                <h1>Nueva Solicitud de Eliminación</h1>
                <p className="solicitud-sub">
                    Descripción detallada de la solicitud de eliminación
                </p>

                {hechoId && (
                    <p className="solicitud-hecho">
                        Hecho seleccionado: <strong>#{hechoId}</strong>
                    </p>
                )}

                <form onSubmit={handleSubmit}>
          <textarea
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              placeholder='"mínimo 200 caracteres"'
              rows={5}
          />

                    <button type="submit" className="btn-enviar">
                        Enviar Solicitud
                    </button>
                </form>
            </div>
        </div>
    );
}
