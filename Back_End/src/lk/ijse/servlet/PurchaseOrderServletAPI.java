package lk.ijse.servlet;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

/**
 * @author : SANDU
 * @created 27/08/2023
 * @project JavaEE_POS_App
 */
@WebServlet(urlPatterns = {"/pages/purchase"})
public class PurchaseOrderServletAPI extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String option = req.getParameter("option");
        switch (option) {
            case "generateOrderID":
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
                    PreparedStatement pstm = connection.prepareStatement("SELECT oid FROM Orders ORDER BY oid DESC LIMIT 1");
                    ResultSet rst = pstm.executeQuery();

                    resp.addHeader("Access-Control-Allow-Origin", "*");

                    resp.addHeader("Content-Type", "application/json");


                    JsonArrayBuilder allOrders = Json.createArrayBuilder();
                    while (rst.next()) {
                        String oId = rst.getString(1);

                        JsonObjectBuilder orderIdObject = Json.createObjectBuilder();
                        orderIdObject.add("oid", oId);


                        allOrders.add(orderIdObject.build());
                    }

                    resp.getWriter().print(allOrders.build());

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "getOrders":
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
                    PreparedStatement pstm = connection.prepareStatement("select * from Orders");
                    ResultSet rst = pstm.executeQuery();

                    resp.addHeader("Access-Control-Allow-Origin", "*");

                    resp.addHeader("Content-Type", "application/json");


                    JsonArrayBuilder allOrders = Json.createArrayBuilder();
                    while (rst.next()) {
                        String  code = rst.getString(1);
                        String id = rst.getString(2);
                        String date = rst.getString(3);
                        String subTotal = rst.getString(4);
                        String discount = rst.getString(5);

                        JsonObjectBuilder orderObject = Json.createObjectBuilder();
                        orderObject.add("code", code);
                        orderObject.add("id", id);
                        orderObject.add("date", date);
                        orderObject.add("subTotal", subTotal);
                        orderObject.add("discount", discount);

                        allOrders.add(orderObject.build());
                    }

                    resp.getWriter().print(allOrders.build());

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "getOrderDetails":
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
                    PreparedStatement pstm = connection.prepareStatement("select * from OrderDetails");
                    ResultSet rst = pstm.executeQuery();

                    resp.addHeader("Access-Control-Allow-Origin", "*");

                    resp.addHeader("Content-Type", "application/json");


                    JsonArrayBuilder allOrderDetails = Json.createArrayBuilder();
                    while (rst.next()) {
                        String oid = rst.getString(1);
                        String cusId = rst.getString(2);
                        String cusName = rst.getString(3);
                        String code = rst.getString(4);
                        String description = rst.getString(5);
                        String qty = rst.getString(6);
                        String price = rst.getString(7);

                        JsonObjectBuilder orderDetailObject = Json.createObjectBuilder();
                        orderDetailObject.add("code", code);
                        orderDetailObject.add("id", oid);
                        orderDetailObject.add("cusName", cusName);
                        orderDetailObject.add("code", code);
                        orderDetailObject.add("description", description);
                        orderDetailObject.add("qty", qty);
                        orderDetailObject.add("price", price);

                        allOrderDetails.add(orderDetailObject.build());
                    }

                    resp.getWriter().print(allOrderDetails.build());

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jsonObject = reader.readObject();
        String oid = jsonObject.getString("oid");
        String cusId = jsonObject.getString("cusID");
        String date = jsonObject.getString("date");
        String subTotal = jsonObject.getString("subTotal");
        String discount = jsonObject.getString("discount");

        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");

        resp.addHeader("Access-Control-Allow-Origin", "*");

        resp.addHeader("Content-Type", "application/json");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");


            connection.setAutoCommit(false);

            PreparedStatement pstm = connection.prepareStatement("insert into Orders values(?,?,?,?,?)");
            pstm.setString(1, oid);
            pstm.setString(2, cusId);
            pstm.setString(3, date);
            pstm.setDouble(4, Double.parseDouble(subTotal));
            pstm.setInt(5, Integer.parseInt(discount));
            if (!(pstm.executeUpdate() > 0)) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new RuntimeException("Order Issue");
            } else {
                JsonArray orderDetails = jsonObject.getJsonArray("orderDetails");

                for (JsonValue orderDetail : orderDetails) {
                    String code = orderDetail.asJsonObject().getString("code");
                    String qty = orderDetail.asJsonObject().getString("qty");
                    String price = orderDetail.asJsonObject().getString("price");


                    PreparedStatement pstms = connection.prepareStatement("insert into OrderDetails values(?,?,?,?,?,?,?)");
                    pstms.setObject(1, code);
                    pstms.setObject(2, cusId);
                    pstms.setObject(3, name);
                    pstms.setObject(4, code);
                    pstms.setObject(5, description);
                    pstms.setObject(6, qty);
                    pstms.setObject(7, price);
                    if (!(pstms.executeUpdate() > 0)) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        throw new RuntimeException("Order Details Issue");
                    }
                }

                connection.commit();
                connection.setAutoCommit(true);

                JsonObjectBuilder response = Json.createObjectBuilder();
                response.add("state", "Success");
                response.add("message", "Order Successfully Purchased..!");
                response.add("data", "");
                resp.setStatus(200);
                resp.getWriter().print(response.build());
            }

        } catch (SQLException | ClassNotFoundException | RuntimeException e) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("state", "Error");
            objectBuilder.add("message", e.getMessage());
            objectBuilder.add("data", "");
            resp.setStatus(500);
            resp.getWriter().print(objectBuilder.build());
        }
    }



    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "PUT");
        resp.addHeader("Access-Control-Allow-Headers", "content-type");
    }
}
