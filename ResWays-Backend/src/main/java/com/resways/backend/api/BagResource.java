package com.resways.backend.api;

import com.resways.backend.dao.BagDao;
import com.resways.backend.dao.UserDao;
import com.resways.backend.model.SurpriseBag;
import com.resways.backend.model.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Random;

@Path("/bags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BagResource {

    private BagDao bagDao = new BagDao();
    private UserDao userDao = new UserDao();

    @GET
    @Path("/available")
    public Response getAvailableBags() {
        List<SurpriseBag> bags = bagDao.findAllAvailable();
        return Response.ok(bags).build();
    }

    @GET
    @Path("/store/{storeId}")
    public Response getStoreBags(@PathParam("storeId") Long storeId) {
        List<SurpriseBag> bags = bagDao.findByStoreId(storeId);
        return Response.ok(bags).build();
    }

    @GET
    @Path("/reserved/{userId}")
    public Response getReservedBags(@PathParam("userId") Long userId) {
        List<SurpriseBag> bags = bagDao.findByReservedById(userId);
        return Response.ok(bags).build();
    }

    @POST
    public Response createBag(SurpriseBag bag) {
        bag.setStatus("Available");
        User restaurant = userDao.findById(bag.getRestaurant().getId());
        bag.setRestaurant(restaurant);
        SurpriseBag saved = bagDao.save(bag);
        return Response.ok(saved).build();
    }

    @POST
    @Path("/{id}/reserve")
    public Response reserveBag(@PathParam("id") Long bagId, User customer) {
        SurpriseBag bag = bagDao.findById(bagId);
        if (bag == null || !bag.getStatus().equals("Available")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Bag not available").build();
        }
        bag.setStatus("Reserved");
        bag.setReservedById(customer.getId());
        String pin = String.format("%04d", new Random().nextInt(10000));
        bag.setReservationCode(pin);
        
        bagDao.update(bag);
        return Response.ok(bag).build();
    }
    
    @POST
    @Path("/{id}/pickup")
    public Response pickupBag(@PathParam("id") Long bagId, SurpriseBag validationRequest) {
        SurpriseBag bag = bagDao.findById(bagId);
        if (bag != null && bag.getStatus().equals("Reserved") && bag.getReservationCode().equals(validationRequest.getReservationCode())) {
            bag.setStatus("Completed");
            bagDao.update(bag);
            return Response.ok(bag).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid pin or bag state").build();
    }
}
