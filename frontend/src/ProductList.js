import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import { Button, Container, Table } from 'reactstrap';
import React, { useEffect, useState } from 'react';
import NavigationBar from './NavigationBar';
import { InputNumber } from 'antd';

const ProductList = () => {

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);

    fetch(serverUrl + '/api/v1/products')
      .then(response => response.json())
      .then(data => {
        setProducts(data.products);
        setLoading(false);
      })
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  function setProductQuantity(quantity, id) {
    products.filter(product => product.id === id)
      .forEach(product => product.quantity = quantity)
  };

  function saveToCart(id) {
    products.filter(product => product.id === id)
      .forEach(product => {
        let quantity = 1;
        if (product.quantity > 1) quantity = product.quantity
        let requestOptions = {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
          },
          body: JSON.stringify({ productId: product.id, quantity: quantity })
        };
        let is401 = false;
        fetch(serverUrl + '/api/v1/cart', requestOptions)
          .then(response => {
            if (response.ok) {
              alert(product.name + " is added to your cart. Quantity: " + quantity);
              return null;
            }
            if (response.status === 401) is401 = true;
            return response.json();
          })
          .then(data => {
            if (data !== null) {
              if (is401 && data.message.includes(jwtExpiredMessage)) {
                refreshToken(() => saveToCart(id));
              } else {
                alert("Failed to add product. Reason: " + data.message);
              }
            }
          });
      })
  };

  const productList = products.map(product => {
    return <tr key={product.id}>
      <td>{product.name}</td>
      <td>{product.description}</td>
      <td>{product.price} USD</td>
      <td>{product.stockQuantity}</td>
      <td>
        <InputNumber min={1} max={product.stockQuantity} defaultValue={1} step={1}
          onChange={(value) => setProductQuantity(value, product.id)} />
      </td>
      <td>
        <Button variant="outline-success" color="success"
          disabled={localStorage.getItem('accessToken') === null}
          onClick={() => saveToCart(product.id)} >To Cart</Button>
      </td>
    </tr>
  });

  return (
    <div>
      <NavigationBar />
      <Container hidden={products.length === 0} fluid>
        <h3 className="center">Products</h3>
        <Table className="mt-4">
          <thead>
            <tr>
              <th width="20%">Name</th>
              <th>Description</th>
              <th width="10%">Price</th>
              <th width="10%">Stock Quantity</th>
              <th width="10%">Items</th>
              <th width="10%">Action</th>
            </tr>
          </thead>
          <tbody>
            {productList}
          </tbody>
        </Table>
      </Container>
      <Container hidden={products.length !== 0} fluid>
        <h3 className="center">No Products</h3>
      </Container>
    </div>
  );
};

export default ProductList;