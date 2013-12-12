# JetBrains JetPad Projectional Editing Framework

Projectional editing is an approach to editing where instead of working with text as is, user works with model data structure
directly. The following widely used applications can be considered projectional editors:
* WYSIWYG text editor
* Spreadsheet applciations
* Diagram editors

The framework implements projectional editor in the style of JetBrains MPS (http://www.jetbrains.com/mps/), with the following
advantages over it:
* Support for web - the framework uses GWT for this purpose
* Testability - it's possible to create fast test for almost any editor.
* Better architecture
It also supports new features which aren't available in MPS:
* Diagrams - it's possible to create diagrams with the framework and mix and match diagrams with MPS-like editors
* Hybrid editing - hybrid approach to expression editing which makes it much more text like while keeping most of the projectional
editor's benefits

This framework is based on JetBrains JetPad Mappers framework (https://github.com/JetBrains/jetpad-mapper). You need to install
its artifacts, if you want to use or participate in the development of the framework.

You can try the following online demo:
* http://jb-proj-demo.appspot.com/ - projectional editing, hybrid editing, diagrams
* http://mbeddr-dataflow.appspot.com/ - data flow editor demo

Modules:
* cell - cells
* projectional - projectional (MPS like) editing
* hybrid - hybrid editors
* dataflow - data flow diagram demo
* demo - demos
* domUtil - dom utilities
* event - commonly used event objects
* grammar - dynamic LR parser generator which is used in projectional editing framework
* view - views for projectional editing. Can be targeted to AWT graphics or SVG in browser

