package rest;

import com.google.gson.Gson;
import entity.User;
import entity.Wish;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import utils.PuSelector;

/**
 * @author lam@cphbusiness.dk
 */
@Path("info")
public class DemoResource {

    Gson gson = new Gson();
    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoForAll() {
        return "{\"msg\":\"Hello anonymous\"}";
    }

    //Just to verify if the database is setup
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public String allUsers() {
        EntityManager em = PuSelector.getEntityManagerFactory("pu").createEntityManager();
        try {
            List<User> users = em.createQuery("select user from User user").getResultList();
            return "[" + users.size() + "]";
        } finally {
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user")
    @RolesAllowed("user")
    public String getFromUser() {
        EntityManager em = PuSelector.getEntityManagerFactory("pu").createEntityManager();
        String thisUser = securityContext.getUserPrincipal().getName();
        List<Wish> wishes = new ArrayList();
        try {
            wishes = em.createQuery("select wish from Wish wish where wish.user.userName = :userName ").setParameter("userName", thisUser).getResultList();
            String wishList = "{\"name\": \"" + thisUser + "\", \"flightWish\":[";
            for (int i = 0; i < wishes.size(); i++) {
                wishList += gson.toJson(wishes.get(i).getWishID()) + ",";
            }
            String outPut = wishList.substring(0, wishList.length() - 1);
            if (wishes.size() > 0) {
                return outPut + "]}";
            } else {
                return "{\"name\":\"" + thisUser + "\",\"flightWish\":\"[]\"}";
            }

        } finally {
            em.close();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/{wish}")
    @RolesAllowed("user")
    public void postUserWish(@PathParam("wish") String wish) {
        EntityManager em = PuSelector.getEntityManagerFactory("pu").createEntityManager();
        String thisUser = securityContext.getUserPrincipal().getName();
        User user = new User(thisUser, "");
        Wish objectWish = new Wish(wish, user);
        try {
            em.getTransaction().begin();
            em.persist(objectWish);

        } finally {
            em.getTransaction().commit();
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("admin")
    @RolesAllowed("admin")
    public String getFromAdmin() {
        String thisUser = securityContext.getUserPrincipal().getName();

        return "{\"name\":\"" + thisUser + "\"}";
    }
}