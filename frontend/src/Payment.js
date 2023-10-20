import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import { useParams, useNavigate } from "react-router-dom";
import React, { useState, useEffect } from 'react';

const Payment = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);

    let requestOptions = {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('accessToken') }
    };
    let error = false;
    let is401 = false;
    fetch(serverUrl + '/api/v1/orders/' + id + "/payment", requestOptions)
      .then(response => {
        if (!response.ok && response.status !== 401) error = true;
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (is401 && data.message.includes(jwtExpiredMessage)) {
          refreshToken(() => window.location.reload());
        } else if (error || is401) {
          alert("Failed to create order payment. Reason: " + data.message);
          navigate('/ui/orders/');
        } else {
          navigate("/ui/redirect/", { state: { url: data.paymentUrl } });
        }
      });
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  return null;
};

export default Payment;
