import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProductList from './ProductList';
import CancelOrder from './CancelOrder';
import PayOrder from './PayOrder';
import Redirect from './Redirect';
import Payment from './Payment';
import Orders from './Orders';
import Cart from './Cart';
import './App.css';

const App = () => {
  return (
    <Router>
      <Routes>
        <Route exact path="/" element={<ProductList />} />
        <Route exact path="/ui/" element={<ProductList />} />
        <Route exact path="/ui/cart/" element={<Cart />} />
        <Route exact path="/ui/orders/" element={<Orders />} />
        <Route exact path="/ui/redirect/" element={<Redirect />} />
        <Route exact path="/ui/orders/:id/pay/" element={<PayOrder />} />
        <Route exact path="/ui/orders/:id/payment/" element={<Payment />} />
        <Route exact path="/ui/orders/:id/cancel/" element={<CancelOrder />} />
      </Routes>
    </Router>
  );
}

export default App;