import { useEffect, useMemo, useState } from 'react';
import { adminAPI } from '../../lib/api';

export default function TransportServicesAdmin() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ seats: '', price_in_dubai: '', price_in_other_city: '', hours: '' });
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await adminAPI.listTransportServices();
      setItems(data.data || []);
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const onCreate = async () => {
    setError('');
    try {
      const payload = {
        seats: Number(form.seats),
        price_in_dubai: Number(form.price_in_dubai),
        price_in_other_city: Number(form.price_in_other_city),
        hours: form.hours ? Number(form.hours) : undefined,
      };
      await adminAPI.createTransportService(payload);
      setForm({ seats: '', price_in_dubai: '', price_in_other_city: '', hours: '' });
      await load();
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    }
  };

  const onSeed = async () => {
    setError('');
    try {
      await adminAPI.seedDefaultTransportServices();
      await load();
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    }
  };

  const onDelete = async (id) => {
    setError('');
    try {
      await adminAPI.deleteTransportService(id);
      await load();
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <h1>Transport Services</h1>
      {error ? <p style={{ color: 'red' }}>{error}</p> : null}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <input placeholder="Seats" value={form.seats} onChange={(e) => setForm({ ...form, seats: e.target.value })} />
        <input placeholder="Price in Dubai" value={form.price_in_dubai} onChange={(e) => setForm({ ...form, price_in_dubai: e.target.value })} />
        <input placeholder="Price in Other City" value={form.price_in_other_city} onChange={(e) => setForm({ ...form, price_in_other_city: e.target.value })} />
        <input placeholder="Hours (optional)" value={form.hours} onChange={(e) => setForm({ ...form, hours: e.target.value })} />
        <button onClick={onCreate}>Create</button>
        <button onClick={onSeed}>Seed Defaults</button>
      </div>

      {loading ? <p>Loading...</p> : null}
      <table border="1" cellPadding="6" cellSpacing="0">
        <thead>
          <tr>
            <th>Seats</th>
            <th>Hours</th>
            <th>Price (Dubai)</th>
            <th>Price (Other City)</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {items.map((row) => (
            <tr key={row._id}>
              <td>{row.seats}</td>
              <td>{row.hours ?? '-'}</td>
              <td>{row.price_in_dubai}</td>
              <td>{row.price_in_other_city}</td>
              <td>
                <button onClick={() => onDelete(row._id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

