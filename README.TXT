# JE: 18/06/2013

# ====================================================================================================
# to compile

$ ant compileall    -> compiles everything
$ ant jar           -> incremental compilation

# ====================================================================================================
# to run

Everything depends on the content of lib/
- if it contains javabdd...jar => this can run AFMU, not TreeSolver
- if it contains libbuddy.X (X depending on the architecture) => the BDD code will be native

$ sh scripts/runtests.sh   -> should do everything

Files are here with the correct timeout and memory size, do not regenerate without 
knowing what you are doing.

# This run specific tests individually

$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc
$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc
$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc

# To generate these types of scripts

$ java -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -f shell -o scripts/testSA-NOP.sh
                    -> generate test files (change arguments).

# ====================================================================================================
# To generate the web site:

$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/cqnoproj.rdf -o CQNoProj.html
$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/ucqproj.rdf -o UCQProj.html
$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/ucqrdfs.rdf -o UCQrdfs.html

# ====================================================================================================
# Query analysis:

Ideally it should be (work in progress):

$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.queryanalysis.Analysis querylog

# ====================================================================================================
# Content:

logback-*.jar => if you put them in lib, recompile and run, you will have more trace

benchmark     description of the benchmark stuff to run

lib           containmenttester.jar -> the framework with all wrappers
              lmusolver.jar -> you guess it
              subsum.jar -> the chilean stuff that works
              afmusolver.jar -> the japanese solver (hopefully non touched)

src the source for QC testing including wrappers
  fr.inrialpes.tyrexmo.queryanalysis -> common tools for manipulating queries
  				     => TODO: add query log analyser
  fr.inrialpes.tyrexmo.testqc.TestContain -> the main class for testing containment (called by the jat above).
  fr.inrialpes.tyrexmo.testqc.ContainmentSolver -> the minimal interface wrapper have t o implement
  fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -> our wrapper around AFMU solver
  fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -> same for L_mu solver
  fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -> same for the SPARQL-Algebra solver

