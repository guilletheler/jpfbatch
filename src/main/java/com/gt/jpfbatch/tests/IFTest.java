/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gt.jpfbatch.tests;

import com.gt.ifepson.commands.PagoCancelDescRecaTique;
import com.gt.ifepson.commands.AbrirTique;
import com.gt.ifepson.commands.SubtotalTique;
import com.gt.ifepson.commands.ImprItemTique;
import com.gt.ifepson.commands.ImprTxtFiscTique;
import com.gt.ifepson.commands.CerrarTique;
import com.gt.ifepson.capa_fisica.OutParam;
import com.gt.ifepson.IfCommand;
import com.gt.ifepson.capa_fisica.serialPort.PortConfig;
import com.gt.ifepson.commands.enums.CalificadorItem;
import com.gt.ifepson.commands.enums.CalificadorPagoCancelDescReca;
import java.io.IOException;
import java.util.Map;
import com.gt.ifepson.fiscaldoc.FiscalDocument;

/**
 *
 * @author guillermot
 */
public class IFTest {
    public static void doTiquetTest() throws IOException {
        
        FiscalDocument ifb = new FiscalDocument();
        
        PortConfig pf = new PortConfig();
        
        pf.setPortName("/dev/ttyS0");
        pf.setStopBits(1);
        pf.setParity(0);
        pf.setBaudRate(9600);
        pf.setDataBits(8);
        
        ifb.setPortConfig(pf);
        
        IfCommand c;

        c = new AbrirTique();

        ifb.addCommand(c);

        c = new ImprTxtFiscTique();

        ((ImprTxtFiscTique) c).setLineaExtra("linea extra");

        ifb.addCommand(c);

        c = new ImprItemTique();

        ((ImprItemTique) c).setBultos(1);
        ((ImprItemTique) c).setCalificador(CalificadorItem.MONTO_AGREGADO_O_VENTA_SUMA);
        ((ImprItemTique) c).setCantidad(0.01d);
        ((ImprItemTique) c).setDescripcionProducto("item de linea");
        ((ImprItemTique) c).setPrecioUnitario(10d);
        ((ImprItemTique) c).setIva(21d);

        ifb.addCommand(c);

        c = new SubtotalTique();

        ifb.addCommand(c);

        c = new PagoCancelDescRecaTique();

        ((PagoCancelDescRecaTique) c).setCalificador(CalificadorPagoCancelDescReca.DESCUENTO);

        ((PagoCancelDescRecaTique) c).setDescripcionEnTique("POR SER MUY FEO");

        ((PagoCancelDescRecaTique) c).setMonto(0.05d);

        ifb.addCommand(c);

        c = new PagoCancelDescRecaTique();

        ((PagoCancelDescRecaTique) c).setCalificador(CalificadorPagoCancelDescReca.SUMA_IMPORTE_PAGADO);

        ((PagoCancelDescRecaTique) c).setDescripcionEnTique("EFECTIVO");

        ((PagoCancelDescRecaTique) c).setMonto(0.05d);

        ifb.addCommand(c);

        c = new CerrarTique();

        ifb.addCommand(c);
        
        ifb.run();
        
        
        // acá vienen todas las respuestas
        // que pueden ser cualquiera de ifepson.doc.OutParam
        // hay que analizarlas para ver si viene con algun error
        
        Map<OutParam, String> respuesta = ifb.getRespuesta();
    }
}
