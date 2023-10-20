import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import { Container, Table, Button } from 'reactstrap';
import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import NavigationBar from './NavigationBar';
import { format } from "date-fns";

const Orders = () => {

  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);

    let requestOptions = {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('accessToken') }
    };
    let is401 = false;
    fetch(serverUrl + '/api/v1/orders', requestOptions)
      .then(response => {
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (is401 && data.message.includes(jwtExpiredMessage)) {
          refreshToken(() => window.location.reload());
        } else {
          setOrders(data.orders);
          setLoading(false);
        }
      });
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  function payOrder(id) {
    navigate("/ui/orders/" + id + "/payment/");
  }

  function cancelOrder(id) {
    navigate("/ui/orders/" + id + "/cancel/");
  }

  const orderList = orders.map(order => {
    return <tr key={order.id}>
      <td>{format(new Date(order.dateTime), 'dd-MM-yyyy HH:mm:ss')}</td>
      <td>{order.status}</td>
      <td>{
        <ul>{
          order.orderItems.map(item => {
            return <li>Name: {item.name}; Price: {item.price} USD; Quantity: {item.quantity}</li>
          })}
        </ul>
      }</td>
      <td>{order.totalPrice} USD</td>
      <td>
        <Button disabled={order.status !== 'NEW'}
          variant="outline-success" color="success"
          onClick={(e) => payOrder(order.id)}>Pay</Button>
        <span> </span>
        <Button disabled={order.status !== 'NEW'}
          variant="outline-success" color="warning"
          onClick={(e) => cancelOrder(order.id)}>Cancel</Button>
      </td>
    </tr>
  });

  return (
    <div>
      <NavigationBar />
      <Container hidden={orders.length === 0} fluid>
        <h3 className="center">Orders</h3>
        <Table className="mt-4">
          <thead>
            <tr>
              <th width="15%">Date</th>
              <th width="10%">Status</th>
              <th>Description</th>
              <th width="15%">Total Price</th>
              <th width="15%">Action</th>
            </tr>
          </thead>
          <tbody>
            {orderList}
          </tbody>
        </Table>
      </Container>
      <Container hidden={orders.length !== 0} fluid>
        <h3 className="center">No orders</h3>
      </Container>
    </div>
  );
};

export default Orders;