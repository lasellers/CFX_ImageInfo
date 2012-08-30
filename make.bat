rem For ColdFusion10. Requires elevated privileges command line.

cd src

javac -classpath C:\ColdFusion10\cfusion\wwwroot\WEB-INF\lib\cfx.jar -verbose CFX_ImageInfo.java -Xlint:deprecation

jar cvf CFX_ImageInfo.jar CFX_ImageInfo.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$BMPH.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$IMAGEDESCRIPTOR.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$PF.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$PNGH.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$R.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$1$TGAH.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$ITEM.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$JPG.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$READ.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$SingleFilenameFilter.class
jar uvf CFX_ImageInfo.jar CFX_ImageInfo$EOFException.class

copy CFX_ImageInfo.jar ..\

cd ..
