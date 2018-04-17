package com.appspot;

import com.googlecode.objectify.ObjectifyService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Created by eljah32 on 4/15/2018.
 */
public class EmailAReportServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("Starting weekly email report");

        List<MeasurementStation> measurementStationList = ObjectifyService.ofy()
                .load()
                .type(MeasurementStation.class)
                .list();

        List<String> measurementParametersLabels = new ArrayList<String>() {
        };
        List<MeasurementParameter> measurementParametersList = ObjectifyService.ofy()
                .load()
                .type(MeasurementParameter.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();
        for (MeasurementParameter st : measurementParametersList) {
            measurementParametersLabels.add(st.parameterName);
        }

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();

        List<List<Measurement>> datasets = new ArrayList<>();

        Set<Date> datesForGrid = new LinkedHashSet<>();

        for (MeasurementStation ms : measurementStationList) {
            for (String parameter : measurementParametersLabels) {
                datasets.add(ObjectifyService.ofy()
                        .load()
                        .type(Measurement.class)
                        .ancestor(ms).
                                filter("dateStart >= ", sevenDaysAgo.getTime()).
                                filter("parameterString = ", parameter).
                                list());
            }
        }

        for (List<Measurement> lsm : datasets) {
            for (Measurement ms : lsm) {
                datesForGrid.add(ms.dateStartDate);
            }
        }

//        System.out.print("Dates\t");
//
//        for (Date date : datesForGrid) {
//            System.out.print(date + "\t");
//
//        }
//        System.out.println();
//        for (List<Measurement> lsm : datasets) {
//            if (lsm.size() > 0 && lsm.get(0) != null) {
//                System.out.print(lsm.get(0).station + "\t");
//                for (Date date : datesForGrid) {
//                    for (Measurement ms : lsm) {
//                        if (ms.dateStartDate.equals(date)) {
//                            System.out.print(ms.value);
//                            if (ms.tlvExceds)
//                            {System.out.print("(!)");}
//                        }
//                        System.out.print("\t");
//                    }
//                }
//                System.out.println();
//            }
//        }

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        CreationHelper createHelper = wb.getCreationHelper();

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(0);

        Cell cell = row.createCell(0);
        cell.setCellValue("Дата и время");

        Cell cell2 = row.createCell(1);
        cell2.setCellValue("ПДК, единицы измерения");
        int i = 1;

        CellStyle cellStyleDate = wb.createCellStyle();
        cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));

        CellStyle cellStyleAlert = wb.createCellStyle();
        cellStyleAlert.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyleAlert.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyleAlert.setBottomBorderColor(IndexedColors.RED.getIndex());

        CellStyle cellStyleAlertApproach = wb.createCellStyle();
        cellStyleAlertApproach.setFillForegroundColor(IndexedColors.PINK.getIndex());
        cellStyleAlertApproach.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyleAlertApproach.setBottomBorderColor(IndexedColors.PINK.getIndex());

        for (Date date : datesForGrid) {
            i++;
            cell = row.createCell(i);
            cell.setCellValue(date);

            cell.setCellStyle(cellStyleDate);
        }

        int rowIndex = 0;


        for (List<Measurement> lsm : datasets) {
            if (lsm.size() > 0 && lsm.get(0) != null) {
                //System.out.print(lsm.get(0).station + "\t");
                rowIndex++;
                row = sheet.createRow(rowIndex);
                System.out.println(lsm.get(0).station.toString() + " " + lsm.size());
                Cell station_and_parameter = row.createCell(0);
                Cell tlv = row.createCell(1);

                int cellIndex = 1;
                Measurement last = null;
                for (Date date : datesForGrid) {
                    cellIndex++;
                    Cell current = row.createCell(cellIndex);
                    for (Measurement ms : lsm) {
                        if (ms.dateStartDate.equals(date)) {
                            current.setCellValue(ms.value);
                            if (ms.tlvApproached != null) {
                                if (ms.tlvApproached)
                                    current.setCellStyle(cellStyleAlertApproach);
                            }
                            if (ms.tlvExceds)
                                current.setCellStyle(cellStyleAlert);
                        }
                        last = ms;
                    }
                }
                if (last != null) {
                    station_and_parameter.setCellValue(last.station.getName() + ':' + last.parameterString);
                    if (last.unit != null) {
                        tlv.setCellValue(last.tlv + ", " + last.unit);
                    } else
                        tlv.setCellValue(last.tlv);
                }
            }
        }


        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("ilya.evlampiev@gmail.com", "Архив измерений загрязнения воздуха"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("eljah@mail.ru", "admin"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("nelbik@gmail.com", "Nelya"));
            //
            msg.setSubject("Недельный архив измерений с " + sevenDaysAgo + " до " + new Date());
            msg.setText("This is a test");

            String htmlBody = "Недельный архив измерений с " + sevenDaysAgo + " до " + new Date();          // ...
            byte[] attachmentData = new byte[]{};  // ...
            Multipart mp = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Недельный архив измерений с " + sevenDaysAgo + " до " + new Date(), "utf-8");
            mp.addBodyPart(textPart);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            mp.addBodyPart(htmlPart);

            MimeBodyPart attachment = new MimeBodyPart();
            ByteArrayOutputStream attachmentDataStream = new ByteArrayOutputStream();
            wb.write(attachmentDataStream);
            attachmentData = attachmentDataStream.toByteArray();
            InputStream attachmentDataStream2 = new ByteArrayInputStream(attachmentData);
            attachment.setFileName("report" + sevenDaysAgo + ".xls");
            attachment.setContent(attachmentDataStream2, "application/vnd.ms-excel");
            mp.addBodyPart(attachment);
            msg.setContent(mp);

            Transport.send(msg);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
