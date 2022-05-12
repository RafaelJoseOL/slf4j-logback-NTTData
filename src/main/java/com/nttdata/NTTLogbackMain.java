package com.nttdata;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NTTLogbackMain {
	// Logger a usar
	
	private static final Logger LOG = LoggerFactory.getLogger(NTTLogbackMain.class);

//	Método main, generamos el lector de XML y cargamos el documento a
//	inspeccionar. También se usará para llamar al método que llenara la tabla de
//	información y la mostrará al finalizar.
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		LOG.info("Inicio del método main");

//		Creamos la tabla de información sobre autores y libros, y una lista de libros
//		con precios erróneos (la tabla procede de la biblioteca de Guava).
		
		Table<String, String, Integer> writersInfo = HashBasedTable.create();
		List<String> booksWithPriceError = new ArrayList<>();

		File xmlFile = new File("data.xml");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = documentBuilder.parse(xmlFile);

		NodeList authorsList = document.getElementsByTagName("author");

		fillTable(authorsList, writersInfo, booksWithPriceError);
		
		for (int i = 0; i <= 50000; i++) {
			LOG.info("Iteración {}", i);
		}

		LOG.info("Datos sobre los escritores, sus libros publicados y el precio de los mismos:");
		for (Table.Cell<String, String, Integer> cell : writersInfo.cellSet()) {
			LOG.debug("Autor: {}. Libro: '{}', precio: {}€.", cell.getRowKey(), cell.getColumnKey(), cell.getValue());
		}
		LOG.info("Libros cuyos precios necesitan revisión:");
		for (int i = 0; i < booksWithPriceError.size(); i++) {
			LOG.info("{}.'{}'.", i + 1, booksWithPriceError.get(i));
		}

		LOG.info("Fin de la ejecución del programa");
	}

	//Método encargado de rellenar la tabla y la lista previamente creadas.
	
	public static void fillTable(NodeList authorsList, Table<String, String, Integer> writersInfo,
			List<String> booksWithPriceError) {
		LOG.info("Inicio del método fillTable, para rellenar la tabla de información de los escritores y sus libros.");
		for (int i = 0; i < authorsList.getLength(); i++) {

			Node node = authorsList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;
				NodeList booksList = element.getElementsByTagName("book");
				String bookAuthor = element.getAttribute("name");

				for (int j = 0; j < booksList.getLength(); j++) {
					String bookTitle = element.getElementsByTagName("book").item(j).getTextContent();
					Integer bookPrice = Integer.parseInt(element.getElementsByTagName("book").item(j).getAttributes()
							.getNamedItem("price").getNodeValue());
					writersInfo.put(bookAuthor, bookTitle, bookPrice);

					if (bookPrice <= 0) {
						LOG.warn(
								"El precio del libro '{}' es igual o inferior a cero, sustituido por un valor estándar y añadido a la lista de libros a revisar.",
								bookTitle);
						writersInfo.put(bookAuthor, bookTitle, 15);
						booksWithPriceError.add(bookTitle);
					} else {
						LOG.info("Libro {} introducido con éxito", bookTitle);
					}
				}
			}
		}
		LOG.info("Fin del método fillTable.");
	}
}
