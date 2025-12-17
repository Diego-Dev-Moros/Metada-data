import { useState } from "react";
import "./ContactoPage.css";

export default function ContactoPage() {
    const [form, setForm] = useState({
        nombre: "",
        apellido: "",
        email: "",
        mensaje: "",
    });
    const [enviando, setEnviando] = useState(false);
    const [ok, setOk] = useState(false);

    const handleChange = (field, value) => {
        setForm((prev) => ({ ...prev, [field]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setEnviando(true);
        setOk(false);

        try {
            // TODO: ajustá la URL al endpoint real de tu back
            // por ahora lo dejamos comentado para que no rompa
            /*
            await fetch("http://localhost:8081/api/contacto", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(form),
            });
            */
            setOk(true);
            setForm({ nombre: "", apellido: "", email: "", mensaje: "" });
        } catch (err) {
            console.error("Error enviando contacto", err);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="contacto-page main-content">
            <div className="contacto-card">
                <h1>Contactános</h1>
                <p className="contacto-sub">
                    Estamos para ayudarte. Completá el formulario y te responderemos a la brevedad.
                </p>

                <form onSubmit={handleSubmit} className="contacto-form">
                    <label>
                        Nombre
                        <input
                            type="text"
                            value={form.nombre}
                            onChange={(e) => handleChange("nombre", e.target.value)}
                            required
                            placeholder='"Ezequiel"'
                        />
                    </label>

                    <label>
                        Apellido
                        <input
                            type="text"
                            value={form.apellido}
                            onChange={(e) => handleChange("apellido", e.target.value)}
                            required
                            placeholder='"Reichel"'
                        />
                    </label>

                    <label>
                        Email
                        <input
                            type="email"
                            value={form.email}
                            onChange={(e) => handleChange("email", e.target.value)}
                            required
                            placeholder='"ejemplo@gmail.com"'
                        />
                    </label>

                    <label>
                        Mensaje
                        <textarea
                            value={form.mensaje}
                            onChange={(e) => handleChange("mensaje", e.target.value)}
                            rows={4}
                            maxLength={500}
                            placeholder="Máximo de 500 caracteres"
                        />
                        <div className="char-count">
                            {form.mensaje.length}/500
                        </div>
                    </label>

                    <button type="submit" disabled={enviando}>
                        {enviando ? "Enviando..." : "Enviar mensaje"}
                    </button>

                    {ok && <p className="ok-msg">✅ Mensaje enviado. ¡Gracias por contactarnos!</p>}
                </form>
            </div>
        </div>
    );
}
