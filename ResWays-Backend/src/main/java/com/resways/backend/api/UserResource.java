package com.resways.backend.api;

import com.resways.backend.dao.BagDao;
import com.resways.backend.dao.UserDao;
import com.resways.backend.model.SurpriseBag;
import com.resways.backend.model.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private UserDao userDao = new UserDao();
    private BagDao bagDao = new BagDao();

    @POST
    @Path("/register")
    public Response register(User user) {
        try {
            // Hash the password before saving
            String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashed);
            
            User saved = userDao.save(user);
            return Response.ok(saved).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Registration failed").build();
        }
    }

    @POST
    @Path("/login")
    public Response login(User credentials) {
        User user = userDao.findByEmail(credentials.getEmail());
        if (user != null && BCrypt.checkpw(credentials.getPassword(), user.getPassword())) {
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
    }

    @GET
    @Path("/{id}/impact")
    public Response getUserImpact(@PathParam("id") Long userId) {
        List<SurpriseBag> bags = bagDao.findByReservedById(userId);
        
        double moneySaved = 0;
        int mealsRescued = bags.size();
        
        for (SurpriseBag bag : bags) {
            moneySaved += (bag.getOldPrice() - bag.getNewPrice());
        }
        
        // Approx 2.5kg of CO2 saved per rescued meal
        double co2Saved = mealsRescued * 2.5;

        Map<String, Object> impact = new HashMap<>();
        impact.put("moneySaved", moneySaved);
        impact.put("mealsRescued", mealsRescued);
        impact.put("co2Saved", co2Saved);

        return Response.ok(impact).build();
    }
}
