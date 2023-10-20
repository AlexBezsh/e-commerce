import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { serverUrl, jwtExpiredMessage, refreshToken } from './Utils';
import React, { useState, useEffect } from 'react';

const PayOrder = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [redirect, setRedirect] = useState(true);

  useEffect(() => {
    setLoading(true);

    let requestOptions = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
      },
      body: JSON.stringify({
        payerId: searchParams.get("PayerID"),
        paymentId: searchParams.get("paymentId")
      })
    };
    let is401 = false;
    fetch(serverUrl + '/api/v1/orders/' + id + "/pay", requestOptions)
      .then(response => {
        if (response.ok) {
          alert("Payment success");
          return null;
        }
        if (response.status === 401) is401 = true;
        return response.json();
      })
      .then(data => {
        if (data !== null) {
          if (is401 && data.message.includes(jwtExpiredMessage)) {
            refreshToken(() => window.location.reload());
            setRedirect(false);
          } else {
            alert("Failed to process payment. Reason: " + JSON.stringify(data));
          }
        }
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  if (redirect) {
    navigate('/ui/orders/');
  }
  return null;
};

export default PayOrder;