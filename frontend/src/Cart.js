import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import { Button, Container, Table } from 'reactstrap';
import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import NavigationBar from './NavigationBar';

const Cart = () => {

  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [totalPrice, setTotalPrice] = useState(0);

  useEffect(() => {
    setLoading(true);

    let requestOptions = {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('accessToken') }
    };
    let is401 = false;
    fetch(serverUrl + '/api/v1/cart', requestOptions)
      .then(response => {
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (is401 && data.message.includes(jwtExpiredMessage)) {
          refreshToken(() => window.location.reload());
        } else {
          setItems(data.cartItems);
          setTotalPrice(data.totalPrice);
          setLoading(false);
        }
      });
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  function removeFromCart(id) {
    let requestOptions = {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('accessToken') }
    };
    let is401 = false;
    fetch(serverUrl + '/api/v1/cart/' + id, requestOptions)
      .then(response => {
        if (response.ok) {
          alert("Item is removed from your cart");
          return null;
        }
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (data !== null) {
          if (is401 && data.message.includes(jwtExpiredMessage)) {
            refreshToken(() => removeFromCart(id));
          } else {
            alert("Failed to remove item. Reason: " + data.message);
          }
        }
      });
    window.location.reload(false);
  };

  function placeOrder() {
    let requestOptions = {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('accessToken') }
    };
    let error = false;
    let is401 = false;
    fetch(serverUrl + '/api/v1/orders', requestOptions)
      .then(response => {
        if (!response.ok && response.status !== 401) error = true;
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (is401 && data.message.includes(jwtExpiredMessage)) {
          refreshToken(() => placeOrder());
        } else if (error || is401) {
          alert('Failed to place new order. Reason: ' + data.message);
        } else {
          navigate('/ui/orders/' + data.id + '/payment/');
        }
      });
  }

  const itemList = items.map(item => {
    return <tr key={item.id}>
      <td>{item.name}</td>
      <td>{item.description}</td>
      <td>{item.price} USD</td>
      <td>{item.quantity}</td>
      <td>
        <Button variant="outline-success" color="danger" onClick={() => removeFromCart(item.id)}>Remove</Button>
      </td>
    </tr>
  });

  return (
    <div>
      <NavigationBar />
      <Container hidden={items.length === 0} fluid>
        <h3 className="center">Items</h3>
        <Table className="mt-4">
          <thead>
            <tr>
              <th width="20%">Product Name</th>
              <th>Description</th>
              <th width="10%">Price</th>
              <th width="10%">Quantity</th>
              <th width="10%">Action</th>
            </tr>
          </thead>
          <tbody>
            {itemList}
          </tbody>
        </Table>
        <Container className="center" fluid>
          <Button variant="outline-success" color="success" onClick={placeOrder}>Pay: {totalPrice} USD</Button>
        </Container>
      </Container>
      <Container hidden={items.length !== 0} fluid>
        <h3 className="center">Cart is empty</h3>
      </Container>
    </div>
  );
};

export default Cart;
