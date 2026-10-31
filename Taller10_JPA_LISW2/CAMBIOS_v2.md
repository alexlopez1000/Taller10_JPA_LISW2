# Cambios v2 (revision contra el enunciado)

Codigo (question-bank-saber-pro):
- CompetencyRecord: nuevo @OneToMany(mappedBy = "competency") -> la entidad asociativa QuestionCompetency queda con
  dos @ManyToOne y dos @OneToMany, tal como describe el Punto 4.4 del enunciado.
- QuestionPersistenceTest (test01, test02) y RelationshipMappingTest (test08): flush() + clear() antes de releer,
  para que la lectura venga de la base de datos y no de la cache de primer nivel.
- RelationshipMappingTest.test06: ahora tambien comprueba que la BASE DE DATOS rechaza author_id y topic_id inexistentes
  (claves foraneas), ademas de la validacion del repositorio. El numero de pruebas sigue siendo 22.

Informe:
- 3.4.2: las opciones estan en MultipleChoiceQuestionRecord (no en QuestionRecord).
- 3.4.3 / Tabla 9 (prueba 6) / 4.3: texto alineado con lo que realmente verifican las pruebas 5 y 6.
- Tabla 9 (prueba 22): "tras limpiar el contexto de persistencia" (antes decia "fuera de la transaccion").
- 3.4.4: menciona CompetencyRecord.questionContributions como segundo @OneToMany.
- Tabla 7: agregadas las tablas topics y competencies.

PENDIENTE (no se pudo ejecutar Maven en el entorno de revision):
- Ejecutar `mvn clean test` y confirmar 22 pruebas en verde; actualizar evidencia/mvn_test_resumen.txt si cambia algo.
- Datos del informe: integrantes (maximo 2), numero de grupo, enlace al repositorio de GitHub,
  nombre de archivo lisw2_taller10_gX.docx y confirmar la plantilla oficial.
- Diagramas PNG: quitar el estereotipo <<entity>> del UML de dominio, marcar la PK compuesta de quiz_questions en el ER
  y recolocar el conector reviewer_id (requieren los .drawio originales).
