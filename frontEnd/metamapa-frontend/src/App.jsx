import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar.jsx";
import Footer from "./components/Footer.jsx";
import HechosPage from "./pages/HechosPage";
import CrearHechoPage from "./pages/CrearHechoPage";
import ColeccionesPage from "./pages/ColeccionesPage";
//import PanelAdminPage from "./pages/PanelAdminPage";
//import SolicitudesPage from "./pages/SolicitudesPage";
import ContactoPage from "./pages/ContactoPage";
import NosotrosPage from "./pages/NosotrosPage";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ColeccionPage from "./pages/ColeccionPage";
import NuevaColeccionPage from "./pages/NuevaColeccionPage";
import NuevaSolicitudEliminacionPage from "./pages/NuevaSolicitudEliminacionPage";
import HechoPage from "./pages/HechoPage";
import SolicitudesEliminacionPage from "./pages/SolicitudesEliminacionPage";
import SolicitudEliminacionPage from "./pages/SolicitudEliminacionPage";
import ReportesHechosPage from "./pages/ReportesHechosPage";
import ReporteHechoPage from "./pages/ReporteHechoPage";
import PerfilPage from "./pages/PerfilPage";
import PerfilAuth0Page from "./pages/PerfilAuth0Page";
import UsuariosPage from "./pages/UsuariosPage";
import UsuarioPage from "./pages/UsuarioPage";
import ImportarDatasetPage from "./pages/ImportarDatasetPage";
import EstadisticasPage from "./pages/EstadisticasPage";
import ProtectedRoute from "./components/ProtectedRoute";
import AuthTestPage from "./pages/AuthTestPage";
import CallbackPage from "./pages/CallbackPage";


function App() {
    const [count, setCount] = useState(0)

    return (
        <>

            <Navbar/>
            <Routes>
                {/* Ruta de callback de Auth0 */}
                <Route path="/callback" element={<CallbackPage/>}/>
                
                {/* Ruta de prueba Auth0 */}
                <Route path="/auth-test" element={<AuthTestPage/>}/>
                
                {/* Rutas públicas - Todos pueden acceder */}
                <Route path="/" element={<HechosPage/>}/>
                <Route path="/hechos/:id" element={<HechoPage/>}/>
                <Route path="/contacto" element={<ContactoPage/>}/>
                <Route path="/nosotros" element={<NosotrosPage/>}/>
                <Route path="/colecciones" element={<ColeccionesPage/>}/>
                <Route path="/colecciones/:id" element={<ColeccionPage />}/>
                <Route path="/estadisticas" element={<EstadisticasPage/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/registro" element={<Register/>}/>
                
                {/* Rutas para visualizadores, contribuyentes y admins - Solicitud de eliminación */}
                <Route path="/solicitudes/nueva" element={<NuevaSolicitudEliminacionPage/>}/>
                
                {/* Rutas para contribuyentes y admins */}
                <Route path="/crear-hecho" element={
                    <ProtectedRoute allowedRoles={["CONTRIBUTOR", "ADMIN"]}>
                        <CrearHechoPage/>
                    </ProtectedRoute>
                }/>
                
                {/* Perfil de Auth0 - Disponible para todos los usuarios autenticados */}
                <Route path="/perfil" element={
                    <ProtectedRoute allowedRoles={[]}>
                        <PerfilAuth0Page/>
                    </ProtectedRoute>
                }/>
                
                {/* Perfil antiguo (con hechos del backend) - Solo CONTRIBUTOR y ADMIN */}
                <Route path="/perfil-completo" element={
                    <ProtectedRoute allowedRoles={["CONTRIBUTOR", "ADMIN"]}>
                        <PerfilPage/>
                    </ProtectedRoute>
                }/>
                
                {/* Rutas exclusivas para administradores */}
                <Route path="/reportes-hechos" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <ReportesHechosPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/reportes-hechos/:id" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <ReporteHechoPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/solicitudes" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <SolicitudesEliminacionPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/solicitudes/:id" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <SolicitudEliminacionPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/usuarios" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <UsuariosPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/usuarios/:id" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <UsuarioPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/colecciones/nueva" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <NuevaColeccionPage/>
                    </ProtectedRoute>
                }/>
                <Route path="/importar-dataset" element={
                    <ProtectedRoute allowedRoles={["ADMIN"]}>
                        <ImportarDatasetPage/>
                    </ProtectedRoute>
                }/>



                {/* Otras rutas las agregás después cuando existan
                <Route path="/panel-admin" element={<PanelAdminPage/>}/>
                <Route path="/solicitudes" element={<SolicitudesPage/>}/>
                <Route path="/login" element={<LoginPage/>}/>*/}
            </Routes>
            <Footer />
        </>
    );
}
    /*
  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Raqueloide</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.jsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>

        <Router>
            <Navbar />
            <Routes>
                <Route path="/" element={<HechosPage />} />
                <Route path="/crear-hecho" element={<CrearHechoPage />} />
                <Route path="/colecciones" element={<ColeccionesPage />} />
                <Route path="/panel-admin" element={<PanelAdminPage />} />
                <Route path="/nosotros" element={<NosotrosPage />} />
                <Route path="/contacto" element={<ContactoPage />} />
                <Route path="/solicitudes" element={<SolicitudesPage />} />
                <Route path="/login" element={<LoginPage />} />
            </Routes>
        </Router>

    </>
  )
  */

export default App
