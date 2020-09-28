package com.erovoutika.systems.DAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Order;
import javax.transaction.Transactional;
import com.erovoutika.systems.SystemsApplication;
import com.erovoutika.systems.entities.CartEntity;
import com.erovoutika.systems.entities.OrderEntity;
import com.erovoutika.systems.entities.PostEntity;
import com.erovoutika.systems.entities.ScheduleEntity;
import com.erovoutika.systems.entities.TheCart;
import com.erovoutika.systems.entities.products;
import com.erovoutika.systems.entities.userModel;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

/**
 * RegistrationImplementation
 */

@Repository
public class SystemDAOB implements SystemDAO {

    private EntityManager entityManager;
    final Logger log = LoggerFactory.getLogger(SystemsApplication.class);

    public SystemDAOB(EntityManager entityManager) {
        this.entityManager = entityManager;

    }

    @Override
    @Transactional
    public void saveUser(userModel user) {

        // get the current hibernate session
        Session currentSession = entityManager.unwrap(Session.class);
        // authorities auth = new authorities();
        // auth.setAuthority("ROLE_USER");
        //

        user.setPassword("{bcrypt}" + encoder().encode(user.getPassword()));
        // "{bcrypt}"+
        // user.setAuthority(auth);
        // save employee
        currentSession.saveOrUpdate(user);

    }

    @Override
    @Transactional
    public boolean isUserExist(userModel user) {
        // TODO Auto-ge final Logger log =
        // LoggerFactory.getLogger(SystemsApplication.class);ession =
        // entityManager.unwrap(Session.class);
        // currentSession.byId();

        return false;
    }

    public static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public void saveProduct(MultipartFile imageFile, products product) {
        Session currentSession = entityManager.unwrap(Session.class);
        final Logger log = LoggerFactory.getLogger(SystemsApplication.class);
        Path currentPath = Paths.get("home");
        log.error(currentPath.toString() + " cPath");
        product.setProductImageLocation(imageFile.getOriginalFilename());
        byte[] bytes;

        try {
            bytes = imageFile.getBytes();
            Path path = Paths.get("").toAbsolutePath();
             String temp=path.toString()+"/src/main/resources/static/images/products/" + imageFile.getOriginalFilename();
            Files.write(Paths.get(temp).toAbsolutePath(), bytes);
           
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentSession.saveOrUpdate(product);

    }


    @Override
    @Transactional
    public List<products> findAllProducts() {

        Session currentSession = entityManager.unwrap(Session.class);
        Query<products> query = currentSession.createQuery("from products");
        List<products> productList = query.getResultList();
        log.error(productList.toString());
        return productList;

    }

    @Override
    @Transactional
    public products findProductById(int theId) {

        Session currentSession = entityManager.unwrap(Session.class);
        Query<products> query = currentSession.createQuery("from products where id=" + theId);
        products theProduct = query.getSingleResult();
        log.error("found "+theProduct.getProductDescription());
        return theProduct;
    }

    @Override
    @Transactional
    public void productDeleteById(int theId) {

        Session currentSession = entityManager.unwrap(Session.class);
        String hql = "DELETE FROM products WHERE id = :theId";
        Query query = currentSession.createQuery(hql);
        log.info("deleting: " + theId);
        query.setParameter("theId", theId);
        int rowCount = query.executeUpdate();
        if (rowCount == 0) {
            System.out.println("No data found to delete");
        } else {
            System.out.println("Your record is deleted");
        }

    }

    @Override
    @Transactional
    public userModel findUserByEmail(String email) {

        Session currentSession = entityManager.unwrap(Session.class);
        Query<userModel> query = currentSession.createQuery("from userModel where email= :email");
        log.info("adding: " + email);
        query.setParameter("email", email);
        userModel user = query.getSingleResult();
        return user;
    }





    
    @Override
    @Transactional
    public void updateCart(String email, int id,int quantity) {

        Session currentSession = entityManager.unwrap(Session.class);
        userModel theUser = findUserByEmail(email);
        products theProduct = findProductById(id);
        if (existInCart(id)) {
            CartEntity temp =fetchCart(id);
            temp.setId(temp.getId());
            temp.setOrderQuantity(temp.getOrderQuantity()+quantity);
            currentSession.saveOrUpdate(temp);
        }else{
            CartEntity tempCart = new CartEntity();
            tempCart.setOrderQuantity(quantity);
            tempCart.setUserId(theUser);
            tempCart.setProductId(theProduct);
            currentSession.saveOrUpdate(tempCart);
    
        }
   
    }

    @Override
    @Transactional
    public List<TheCart> cartList(String email) {
        userModel theUser = findUserByEmail(email);
        // TODO Auto-generated method stub
        Session currentSession = entityManager.unwrap(Session.class);
        // Query<CartEntity> query = currentSession.createQuery("productId from
        // CartEntity where userId= :theId");
        Query<TheCart> query = currentSession.createQuery(" from TheCart where userId= :theId");
        log.info("adding: " + email);
        query.setParameter("theId", theUser.getId());
        List<TheCart> productList = query.getResultList();
        log.info(String.valueOf(productList.size()));
        return productList;
    }

    @Override
    @Transactional
    public void order(int quantity, int productId, String email) {
        UUID idOne = UUID.randomUUID();
        int price;
        Session currentSession = entityManager.unwrap(Session.class);
        OrderEntity theOrder = new OrderEntity();
        products product = findProductById(productId);
        userModel user = findUserByEmail(email);
        price = product.getProductPrice() * quantity;
        theOrder.setUuID(UUID.randomUUID().toString());
        theOrder.setOrderQuantity(quantity);
        theOrder.setPrice(price);
        theOrder.setUserId(user);
        theOrder.setProductId(product);
        currentSession.saveOrUpdate(theOrder);

    }

    @Override
    @Transactional
    public void cOrder(Principal pr) {
        Session currentSession = entityManager.unwrap(Session.class);
        
        UUID idOne = UUID.randomUUID();
        String temp = UUID.randomUUID().toString();
        userModel user = findUserByEmail(pr.getName());
        int price;
        List<TheCart> theCart = cartList(pr.getName());
    
        log.error(theCart.size()+" cartsize");
        for (TheCart theProduct : theCart) {
            OrderEntity theOrder = new OrderEntity();
            products product = findProductById(theProduct.getId());
            price = product.getProductPrice() * theProduct.getOrderQuantity();
            theOrder.setOrderId(0);
            theOrder.setUuID(temp);
            log.error(theProduct.getOrderQuantity()+" orderQuant");
            theOrder.setOrderQuantity(theProduct.getOrderQuantity());
            theOrder.setPrice(price);
            theOrder.setUserId(user);
            theOrder.setProductId(product);
            currentSession.saveOrUpdate(theOrder);
        }
        String hql = "DELETE FROM CartEntity WHERE userId = :theId";
        Query query = currentSession.createQuery(hql);
        // log.info("deleting: " + theI);
        query.setParameter("theId", user);
        int rowCount = query.executeUpdate();
        if (rowCount == 0) {
            System.out.println("No data found to delete");
        } else {
            System.out.println("Your record is deleted");
        }

    }

    @Override
    @Transactional
    public void addSchedule(ScheduleEntity theSchedule) {
        Session currentSession = entityManager.unwrap(Session.class);
        currentSession.saveOrUpdate(theSchedule);

    }
    @Override
    public void savePost(MultipartFile imageFile, PostEntity post) throws Exception {
        Session currentSession = entityManager.unwrap(Session.class);
        // authorities auth = new authorities();
        // auth.setAuthority("ROLE_USER");
        final Logger log = LoggerFactory.getLogger(SystemsApplication.class);
        Path currentPath = Paths.get("home");
        log.error(currentPath.toString() + " cPath");
        // String folder = "/photos/";
        // Path absolutePath = currentPath.toAbsolutePath();
        post.setPostBanner(imageFile.getOriginalFilename());
        byte[] bytes;
        try {
            bytes = imageFile.getBytes();
                    Path path = Paths.get("").toAbsolutePath();
                    String temp=path.toString()+"/src/main/resources/static/images/posts/" + imageFile.getOriginalFilename();
                   Files.write(Paths.get(temp).toAbsolutePath(), bytes);
            Files.write(path, bytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // "{bcrypt}"+
        // user.setAuthority(auth);
        // save employee
        post.setEnabled(1);
        currentSession.saveOrUpdate(post);

    }
    @Override
    @Transactional
    public boolean existInCart(int productId) {
        // TODO Auto-generated method stub
        Session currentSession = entityManager.unwrap(Session.class);
        Query<products> query = currentSession.createQuery("from CartEntity where productId= :prod");
        query.setParameter("prod", findProductById(productId));
        try {
            query.getSingleResult();
            return true;
        } catch (Exception e) {
            
            return false;
        }
         
    }

    @Override
    @Transactional
    public CartEntity fetchCart(int productId) {
        Session currentSession = entityManager.unwrap(Session.class);
        Query<CartEntity> query = currentSession.createQuery("from CartEntity where productId= :prod");
        query.setParameter("prod", findProductById(productId));
        CartEntity theProduct = query.getSingleResult();
        return theProduct;

       
    }

    @Override
    @Transactional
    public void archivePost(int postId) {
        Session currentSession = entityManager.unwrap(Session.class);
       PostEntity thePost=findPostbyId(postId);
       thePost.setEnabled(0);
       currentSession.saveOrUpdate(thePost);
    }

    @Override
    @Transactional
    public List<PostEntity> findAllPost() {
        Session currentSession = entityManager.unwrap(Session.class);
        Query<PostEntity> query = currentSession.createQuery("from PostEntity where enabled=1");
        List<PostEntity> postList = query.getResultList();
        log.error(postList.toString());
        return postList;
    }


    @Override
    @Transactional
    public PostEntity findPostbyId(int Id) {
        Session currentSession = entityManager.unwrap(Session.class);
        Query<PostEntity> query = currentSession.createQuery("from PostEntity where id=" + Id);
        PostEntity thePost = query.getSingleResult();
        return thePost;
    }
    



    

   
}