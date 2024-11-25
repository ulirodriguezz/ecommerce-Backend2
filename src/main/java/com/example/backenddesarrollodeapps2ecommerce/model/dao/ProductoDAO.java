package com.example.backenddesarrollodeapps2ecommerce.model.dao;

import com.example.backenddesarrollodeapps2ecommerce.model.entities.BalanceEntity;
import com.example.backenddesarrollodeapps2ecommerce.model.entities.CompraEntity;
import com.example.backenddesarrollodeapps2ecommerce.model.entities.ProductoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public class ProductoDAO{
    @PersistenceContext
    private EntityManager em;
    @Autowired
    IProductoDAOBase daoBase;
    @Autowired
    BalanceDAO balanceDAO;
    @Autowired
    ComprasDAO comprasDAO;
    @Transactional
    public List<ProductoEntity> findAll(int page, int pageSize) {
        int PAGESIZE =(int) daoBase.count(); //CAMBIAR PARA PAGINACION
        int startItem = 0;
        if(page != 1)
        startItem = page  * PAGESIZE + 1 ;
        Session session = em.unwrap(Session.class);
        Query query = session.createQuery("FROM ProductoEntity AS p WHERE  p.idProducto > 0 AND p.stockActual > 0 order by p.idProducto ASC ", ProductoEntity.class);
        query.setMaxResults(PAGESIZE).setFirstResult(startItem);
        if(query.getResultList().isEmpty())
            throw new EmptyResultDataAccessException("No hay productos",1);
        return query.getResultList();

    }
    @Transactional
    public List<ProductoEntity> buscarEnAlerta(int page, int pageSize) {
            int PAGESIZE =(int) daoBase.count(); //CAMBIAR PARA PAGINACION
            int startItem = 0;
            if(page != 1)
            startItem = page  * PAGESIZE + 1 ;
            Session session = em.unwrap(Session.class);
            Query query = session.createQuery("FROM ProductoEntity AS p WHERE p.stockActual <= p.stockMinimo order by p.idProducto ASC ", ProductoEntity.class);
            query.setMaxResults(PAGESIZE).setFirstResult(startItem);
            if(query.getResultList().isEmpty())
                throw new EmptyResultDataAccessException("No hay productos en alerta",1);
            return query.getResultList();


    }
    @Transactional
    public void update(ProductoEntity nuevoProd, long idProd, String username)
    {
        Session session = em.unwrap(Session.class);

        ProductoEntity prodDB = session.getReference(ProductoEntity.class,idProd);
        if(nuevoProd.getCategoria() != null)
            prodDB.setCategoria(nuevoProd.getCategoria());
        if(nuevoProd.getDescripcion() != null)
            prodDB.setDescripcion(nuevoProd.getDescripcion());
        if(nuevoProd.getDescuentoEfectivo() != null)
            prodDB.setDescuentoEfectivo(nuevoProd.getDescuentoEfectivo());
        if(nuevoProd.getDescuentoNoSocios() != null)
            prodDB.setDescuentoNoSocios(nuevoProd.getDescuentoNoSocios());
        if(nuevoProd.getDescuentoSocios() != null)
            prodDB.setDescuentoSocios(nuevoProd.getDescuentoSocios());
        if(nuevoProd.getPrecioCompra() != null)
            prodDB.setPrecioCompra(nuevoProd.getPrecioCompra());
        if(nuevoProd.getPrecioVenta() != null)
            prodDB.setPrecioVenta(nuevoProd.getPrecioVenta());
        if(nuevoProd.getTalles() != null)
            prodDB.setTalles(nuevoProd.getTalles());
        if(nuevoProd.getNombre() != null)
            prodDB.setNombre(nuevoProd.getNombre());
        if(nuevoProd.getStockActual() != null)
        {
            if(prodDB.getStockActual() < nuevoProd.getStockActual()) {
                CompraEntity compra = new CompraEntity();
                int cantidad = nuevoProd.getStockActual() - prodDB.getStockActual();
                compra.setIdProducto(prodDB.getIdProducto());
                compra.setFechaOrden(LocalDate.now());
                compra.setCantidadDeUnidades(cantidad);
                compra.setMontoTotal(nuevoProd.getPrecioCompra() * cantidad);
                compra.setNombreUsuario(username);
                BalanceEntity balance = balanceDAO.getBalance();
                session.persist(compra);
                balance.setMontoCompras(balance.getMontoCompras() + compra.getMontoTotal());
                session.persist(balance);

            }
            //CUANDO HAGAMOS LA ORDEN DE COMPRA ESTO HAY QUE SACARLO DE ACÁ
            prodDB.setStockActual(nuevoProd.getStockActual());

        }
        if(nuevoProd.getStockMinimo() != null)
            prodDB.setStockMinimo(nuevoProd.getStockMinimo());
        if(nuevoProd.getCaracteristicas() != null)
            prodDB.setCaracteristicas(nuevoProd.getCaracteristicas());
        if(nuevoProd.getIdProveedor() != null)
            prodDB.setIdProveedor(nuevoProd.getIdProveedor());
        session.persist(prodDB);
    }
    @Transactional
    public void save(ProductoEntity p) {
        try {
            daoBase.save(p);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Transactional
    public ProductoEntity getPorID(long id)
    {
        try{
           Session sesionActual =  em.unwrap(Session.class);
           ProductoEntity resultado = sesionActual.find(ProductoEntity.class,id);
           return resultado;
        }catch (Throwable e)
        {
            throw new Error("Error interno");
        }
    }
    @Transactional
    public void deleteProducto(long idProducto) {
        Session sesionActual =  em.unwrap(Session.class);
        ProductoEntity prod = sesionActual.find(ProductoEntity.class,idProducto);
        if(prod == null)
            throw new EmptyResultDataAccessException("El producto no existe",1);
        sesionActual.remove(prod);
    }
    private void registrarCompra(ProductoEntity nuevoProd,ProductoEntity prodDB,Session session){
        CompraEntity compra = new CompraEntity();
        int cantidad = nuevoProd.getStockActual() - prodDB.getStockActual();
        compra.setIdProducto(prodDB.getIdProducto());
        compra.setFechaOrden(LocalDate.now());
        compra.setCantidadDeUnidades(cantidad);
        compra.setMontoTotal(nuevoProd.getPrecioCompra() * cantidad);
        compra.setNombreUsuario("luka");
        BalanceEntity balance = balanceDAO.getBalance();
        session.persist(compra);
        balance.setMontoCompras(balance.getMontoCompras() + compra.getMontoTotal());
        session.persist(balance);
    }
}
