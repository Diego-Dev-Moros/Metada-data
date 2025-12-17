import { FaInstagram, FaFacebook, FaTwitter, FaXTwitter } from "react-icons/fa6";

export default function Footer() {
    return (
        <footer className="footer-metamapa mt-5 text-dark">
        <div className="container py-4">
                <div className="row">
                    {/* Columna 1 */}
                    <div className="col-md-4 mb-3">
                        <h5>MetaMapa</h5>
                        <p>Un sistema de código abierto para el mapeo colaborativo</p>
                        <div className="d-flex gap-3">
                            <a href="https://www.instagram.com/pedro.amor7/" target="_blank" rel="noopener noreferrer">
                                <FaInstagram size={24} color="#E4405F" />
                            </a>
                            <a href="https://facebook.com" target="_blank" rel="noopener noreferrer">
                                <FaFacebook size={24} color="#1877F2" />
                            </a>
                            <a href="https://x.com" target="_blank" rel="noopener noreferrer">
                                <FaXTwitter size={24} color="#000" />
                            </a>
                        </div>
                    </div>

                    {/* Columna 2 */}
                    <div className="col-md-4 mb-3">
                        <h5>Enlaces rápidos</h5>
                        <ul className="list-unstyled">
                            <li><a href="#" className="text-dark text-decoration-none">Acerca de nosotros</a></li>
                            <li><a href="#" className="text-dark text-decoration-none">Ver el código (GitHub)</a></li>
                            <li><a href="#" className="text-dark text-decoration-none">Términos de uso</a></li>
                            <li><a href="#" className="text-dark text-decoration-none">Política de privacidad</a></li>
                        </ul>
                    </div>

                    {/* Columna 3 */}
                    <div className="col-md-4 mb-3">
                        <h5>Contacto</h5>
                        <ul className="list-unstyled">
                            <li>📞 Teléfono: +54 011 4902-5831</li>
                            <li>📧 Email: contacto@metamapa.org</li>
                            <li>📍 Dirección: Av. Medrano 951, CABA</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="bg-dark text-light text-center py-2">
                <small>
                    Copyright © 2025 MetaMapa.org – Administrado por el Grupo 7
                </small>
            </div>
        </footer>
    );
}
