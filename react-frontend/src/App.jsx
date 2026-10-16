import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/products';

function App() {
  const [products, setProducts] = useState([]);
  const [formData, setFormData] = useState({ id: null, nev: '', ar: '', kategoria: '' });
  const [isEditing, setIsEditing] = useState(false);

  // Termékek betöltése a H2 adatbázisból (READ)
  const fetchProducts = async () => {
    try {
      const res = await axios.get(API_BASE);
      setProducts(res.data);
    } catch (err) {
      console.error('Hiba a betöltéskor:', err);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  // Űrlap mezők kezelése
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Mentés: Új hozzáadása (CREATE) vagy Módosítás (UPDATE)
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isEditing) {
        await axios.put(`${API_BASE}/${formData.id}`, {
          nev: formData.nev,
          ar: parseFloat(formData.ar),
          kategoria: formData.kategoria
        });
      } else {
        await axios.post(API_BASE, {
          nev: formData.nev,
          ar: parseFloat(formData.ar),
          kategoria: formData.kategoria
        });
      }
      setFormData({ id: null, nev: '', ar: '', kategoria: '' });
      setIsEditing(false);
      fetchProducts();
    } catch (err) {
      console.error('Hiba mentéskor:', err);
    }
  };

  // Módosítás kiválasztása
  const handleEdit = (prod) => {
    setFormData({
      id: prod.id,
      nev: prod.nev,
      ar: prod.ar,
      kategoria: prod.kategoria
    });
    setIsEditing(true);
  };

  // Törlés (DELETE)
  const handleDelete = async (id) => {
    if (window.confirm('Biztosan törölni szeretnéd ezt a terméket?')) {
      try {
        await axios.delete(`${API_BASE}/${id}`);
        fetchProducts();
      } catch (err) {
        console.error('Hiba törléskor:', err);
      }
    }
  };

  const handleCancel = () => {
    setFormData({ id: null, nev: '', ar: '', kategoria: '' });
    setIsEditing(false);
  };

  return (
      <div
          style={{ backgroundColor: '#0b0f19', minHeight: '100vh', color: '#e2e8f0' }}
          className="d-flex flex-column justify-content-between py-4"
      >
        <div className="container">
          {/* Fejléc: Vissza gomb és Cím */}
          <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <a
                href="http://localhost:8080/"
                className="btn btn-outline-light btn-sm px-3 py-2"
                style={{ borderColor: 'rgba(255,255,255,0.2)' }}
            >
              &larr; Vissza a Főoldalra
            </a>
            <h2 className="text-light fw-bold m-0 text-center flex-grow-1">
              React CRUD Kezelőfelület
            </h2>
            <div style={{ width: '150px' }} className="d-none d-md-block"></div>
          </div>

          <div className="row g-4">
            {/* Űrlap */}
            <div className="col-lg-4">
              <div
                  className="card shadow-lg p-4"
                  style={{ backgroundColor: '#111827', border: '1px solid rgba(255,255,255,0.1)' }}
              >
                <h4 className="text-light mb-3">{isEditing ? 'Termék módosítása' : 'Új termék felvétele'}</h4>
                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label className="form-label text-secondary small">Név</label>
                    <input
                        type="text"
                        name="nev"
                        className="form-control"
                        style={{ backgroundColor: '#1f2937', color: '#fff', borderColor: '#374151' }}
                        value={formData.nev}
                        onChange={handleChange}
                        required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label text-secondary small">Ár (Ft)</label>
                    <input
                        type="number"
                        step="any"
                        name="ar"
                        className="form-control"
                        style={{ backgroundColor: '#1f2937', color: '#fff', borderColor: '#374151' }}
                        value={formData.ar}
                        onChange={handleChange}
                        required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label text-secondary small">Kategória</label>
                    <input
                        type="text"
                        name="kategoria"
                        className="form-control"
                        style={{ backgroundColor: '#1f2937', color: '#fff', borderColor: '#374151' }}
                        value={formData.kategoria}
                        onChange={handleChange}
                        required
                    />
                  </div>

                  <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-primary flex-grow-1">
                      {isEditing ? 'Frissítés' : 'Hozzáadás'}
                    </button>
                    {isEditing && (
                        <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                          Mégse
                        </button>
                    )}
                  </div>
                </form>
              </div>
            </div>

            {/* Lista */}
            <div className="col-lg-8">
              <div
                  className="card shadow-lg p-4"
                  style={{ backgroundColor: '#111827', border: '1px solid rgba(255,255,255,0.1)' }}
              >
                <h4 className="text-light mb-3">Termékek listája</h4>
                {products.length === 0 ? (
                    <p className="text-secondary mb-0">Nincs még rögzített termék az adatbázisban.</p>
                ) : (
                    <div className="table-responsive">
                      <table className="table table-dark table-hover mb-0" style={{ backgroundColor: 'transparent' }}>
                        <thead>
                        <tr>
                          <th>ID</th>
                          <th>Név</th>
                          <th>Ár</th>
                          <th>Kategória</th>
                          <th className="text-end">Műveletek</th>
                        </tr>
                        </thead>
                        <tbody>
                        {products.map((p) => (
                            <tr key={p.id}>
                              <td>{p.id}</td>
                              <td>{p.nev}</td>
                              <td>{p.ar} Ft</td>
                              <td>{p.kategoria}</td>
                              <td className="text-end">
                                <button className="btn btn-sm btn-outline-info me-2" onClick={() => handleEdit(p)}>
                                  Módosítás
                                </button>
                                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(p.id)}>
                                  Törlés
                                </button>
                              </td>
                            </tr>
                        ))}
                        </tbody>
                      </table>
                    </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Lábléc: Név és Neptun kód */}
        <footer className="text-center text-secondary pt-4 pb-2 mt-5 border-top" style={{ borderColor: 'rgba(255,255,255,0.1)' }}>
          <p className="mb-1 text-light">
            <strong>Készítők:</strong> Horváth Gréta - AZQ13J &amp; Dibáczi Eszter - AF96RG
          </p>
        </footer>
      </div>
  );
}

export default App;