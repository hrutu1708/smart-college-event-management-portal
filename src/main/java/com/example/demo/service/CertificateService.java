package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

@Service
public class CertificateService {

    public byte[] generateCertificate(
            String studentName,
            String eventTitle,
            String eventDate) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            PDDocument document =
                    new PDDocument();

            PDPage page =
                    new PDPage();

            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(
                            document,
                            page
                    );

            // Title
            content.beginText();

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    ),
                    26
            );

            content.newLineAtOffset(
                    150,
                    700
            );

            content.showText(
                    "CERTIFICATE OF PARTICIPATION"
            );

            content.endText();


            // Certificate text
            content.beginText();

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    ),
                    16
            );

            content.newLineAtOffset(
                    100,
                    600
            );

            content.showText(
                    "This certificate is proudly presented to"
            );

            content.endText();


            // Student name
            content.beginText();

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    ),
                    22
            );

            content.newLineAtOffset(
                    150,
                    550
            );

            content.showText(
                    studentName
            );

            content.endText();


            // Event information
            content.beginText();

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    ),
                    16
            );

            content.newLineAtOffset(
                    100,
                    480
            );

            content.showText(
                    "for successfully participating in"
            );

            content.newLineAtOffset(
                    0,
                    -40
            );

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    ),
                    20
            );

            content.showText(
                    eventTitle
            );

            content.newLineAtOffset(
                    0,
                    -40
            );

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    ),
                    14
            );

            content.showText(
                    "Event Date: " + eventDate
            );

            content.newLineAtOffset(
                    0,
                    -60
            );

            content.showText(
                    "Certificate Date: " +
                    LocalDate.now()
            );

            content.endText();


            content.close();

            document.save(output);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Certificate generation failed: "
                    + e.getMessage()
            );
        }
    }
}