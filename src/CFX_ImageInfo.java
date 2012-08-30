/*
See make.bat for compilation instructions.
*/

//package com.intrafoundation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import com.allaire.cfx.CustomTag;
import com.allaire.cfx.Query;
import com.allaire.cfx.Request;
import com.allaire.cfx.Response;

/**
 *
 *
 *
 * CFX_ImageInfo
 * Works with all Java versions of ColdFusion MX and up.
 *
 * This Java language ColdFusion extension tag (that is, a CFX) for Adobe ColdFusion MX and up, CFX_ImageInfo, has one simple purpose:
 * It returns information such as width, height, colors, comments, compression type, dpi, frames, etc about graphic files.
 * The file formats it handles include TGA, GIF*, JPG, PNG, BMP, PCX, TIFF, and PSD.
 *
 * You can either ask it to return information about a single file or about all (known graphic) files in a specified folder.
 * When using the folder option you can also specify a filter mask.
 *
 * The source code to this program is included as open source.
 * It has been tested under Windows only at the moment.
 * If you have compiled and used it on other platforms please feel free to drop a note.
 *
 * Edited in Eclipse.
 *
 * This obsoletes the original 32-bit C++ version.
 *
 * @author Lewis A. Sellers (min)
 * @version 4.0a
 * @see #READ
 * @see #ITEM
 * @see #EOFException
 **/
public class CFX_ImageInfo implements CustomTag {
	READ read = new READ();
	private ITEM item = new ITEM();
	JPG jpg = new JPG();
	Query query = null;
	
	/**
	 * Names of all fields returned from a query.
	 */
	final String[] columns = { "File", "Path", "Type", "LastModified", "Size", "Error", "Format", "Width", "Height", "Pixels", "PixelBits", "Colors", "ColorType", "Compression", "DPI", "Comments", "Layers", "Duration", "TransparencyIndex", "Transparency" };
	final int iFile = 1;
	final int iPath = 2;
	final int iType = 3;
	final int iLastModified = 4;
	final int iSize = 5;
	final int iError = 6;
	final int iFormat = 7;
	final int iWidth = 8;
	final int iHeight = 9;
	final int iPixels = 10;
	final int iPixelBits = 11;
	final int iColors = 12;
	final int iColorType = 13;
	final int iCompression = 14;
	final int iDPI = 15;
	final int iComments = 16;
	final int iLayers = 17;
	final int iDuration = 18;
	final int iTransparencyIndex = 19;
	final int iTransparency = 20;
	
	/**
	 * Class that contains query field data temporarily.
	 */
	class ITEM {
		String File;
		String Path;
		String Type;
		String Error;
		int Width;
		int Height;
		int Pixels;
		long Size; //i64
		int PixelBits;
		long Colors; //i64
		int DPI;
		String Compression;
		String ColorType;
		String Comments;
		int Layers;
		long Duration;
		String LastModified;
		long TransparencyIndex;
		long Transparency;
		String Format;
	}
	
	
	/**
	 *
	 *
	 *
	 **/
	public class SingleFilenameFilter implements FilenameFilter {
		public SingleFilenameFilter(String filter) {
			this.filter = filter;
		}
		
		private String filter;
		
		public boolean accept(File dir, String name) {
			if (name.endsWith(filter))
				return true;
			else
				return false; // (new File(dir, name)).isDirectory();
		}
	}
	
	/**
	 *
	 *
	 *
	 * Main processing method for custom tag
	 **/
	public void processRequest(Request request, Response response) throws Exception {
		try {
			//
			response.setVariable("ImageInfoDescription", "OPEN SOURCE. FREEWARE. Java. Returns information on TGA,GIF,JPG/JFIF,PNG,BMP,PCX,TIFF and PSD, etc images. Written by Lewis A. Sellers, webmaster@intrafoundation.com. Copyright (c) 1999, 2000, 2003, 2012. Intrafoundation Software, http://www.intrafoundation.com.");
			response.setVariable("ImageInfoVersion", "4.0a, August 8th 2012");
			
			//
			response.setVariable("ImageInfoFolder", "");
			response.setVariable("ImageInfoFile", "");
			response.setVariable("ImageInfoFilter", "");
			
			//
			String szfolder = request.getAttribute("FOLDER");
			String szfile = request.getAttribute("FILE");
			String szfilter = request.getAttribute("FILTER");
			
			if (szfolder == null)
				szfolder = "";
			
			if (szfile == null)
				szfile = "";
			
			if (szfilter == null)
				szfilter = "";
			
			response.setVariable("ImageInfoFile", szfile);
			response.setVariable("ImageInfoFolder", szfolder);
			response.setVariable("ImageInfoFilter", szfilter);
			
			//
			query = response.addQuery("ImageInfo", columns);
			
			//
			if ((szfolder.length() == 0) && (szfile.length() == 0))
				;
			else if (szfolder.indexOf("..") != -1)
				log_error(iGRAMMAR, "Relative folder locations not permitted in FOLDER.");
			else if ((szfolder.indexOf("*") != -1) || (szfolder.indexOf("?") != -1))
				log_error(iGRAMMAR, "Wildcard characters (* or ?) not permitted in FOLDER.");
			else if ((szfile.indexOf("*") != -1) || (szfile.indexOf("?") != -1))
				log_error(iGRAMMAR, "Wildcard characters (* or ?) not permitted in FILE.");
			else if ((szfilter.indexOf("*") != -1) || (szfilter.indexOf("?") != -1))
				log_error(iGRAMMAR, "Wildcard characters (* or ?) not permitted in FILTER.");
			else if ((szfilter.length() > 0) && (szfolder.length() == 0))
				log_error(iGRAMMAR, "FILTER must be used with FOLDER.");
			else if (szfile.length() > 0) { //file
				int i = szfile.lastIndexOf('\\');
				if (i > 0) {
					String sfolder = szfile.substring(0, i) + "\\";
					String sfilter = szfile.substring(i + 1);
					String ss = sfolder + sfilter;
					imageInfo(ss, request, response);
				}
				else
					imageInfo(szfile, request, response);
			}
			else if (szfilter.length() > 0) { //filter
				try {
					File f = new File(szfolder);
					try {
						FilenameFilter sff = new SingleFilenameFilter(szfilter);
						File[] subf = f.listFiles(sff);
						for (int i = 0; i < subf.length; i++)
							imageInfo(subf[i].getAbsolutePath(), request, response);
					}
					catch (SecurityException e) {
						log_error(iSECURITY, "File permissions do not allow access to this file/folder.");
					}
				}
				catch (NullPointerException e) {
					log_error(iFILEDOESNOTEXIST, "Invalid handle while getting directory (" + szfolder + ").");
				}
			}
			else if (szfolder.length() > 0) { //folder
				try {
					File f = new File(szfolder);
					try {
						File[] subf = f.listFiles();
						for (int i = 0; i < subf.length; i++)
							imageInfo(subf[i].getAbsolutePath(), request, response);
					}
					catch (SecurityException e) {
						log_error(iSECURITY, "File permissions do not allow access to this file/folder.");
					}
				}
				catch (NullPointerException e) {
					log_error(iFILEDOESNOTEXIST, "Invalid handle while getting directory (" + szfolder + ").");
				}
			}
			
			//
			response.setVariable("ImageInfoError", strError);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 **/
	private String to2(int n) {
		String tmp=Integer.toString(n+100);
		return tmp.substring(1,3);
	}
	
	/**
	 **/
	private String to4(int n) {
		String tmp=Integer.toString(n+10000);
		return tmp.substring(1,5);
	}
	
	/**
	 *
	 *
	 *
	 * This converts a standard "long" second count from 1970 into the SQL ODBC
	 * format timestamp:
	 * "{ts '%04d-%02d-%02d %02d:%02d:%02d'}",
	 * @returns: ODBC String of ts format
	 **/
	@SuppressWarnings("deprecation")
	private String longToSQLTS(long t) {
		Date d = new Date(t);
		String tmp = "{ts '";
		tmp += to4(d.getYear()+1900) + "-";
		tmp += to2(d.getMonth()) + "-";
		tmp += to2(d.getDay()) + " ";
		tmp += to2(d.getHours()) + ":";
		tmp += to2(d.getMinutes()) + ":";
		tmp += to2(d.getSeconds()) + "}";
		return tmp;
	}
	
	/**
	 *
	 **/
	private void imageInfo(String szfile, Request request, Response response) {
		try {
			File f = new File(szfile);
			if (f.isFile()) {
				// file
				item.Error = "";
				item.File = f.getName();
				item.Path = f.getAbsolutePath();
				item.LastModified = longToSQLTS(f.lastModified());
				item.Size = f.length();
				item.Type = "";
				item.Format = "";
				item.Width = 0;
				item.Height = 0;
				item.Pixels = 0;
				item.PixelBits = 0;
				item.Colors = 0;
				item.ColorType = "";
				item.Compression = "";
				item.Comments = "";
				item.DPI = 0;
				item.Layers = 0;
				item.Duration = 0;
				item.TransparencyIndex = 0;
				item.Transparency = 0;
				
				//
				if (!read.open(szfile))
					log_error(iFILEDOESNOTEXIST, "File could not be opened for reading (" + szfile + ").");
				else {
					//
					boolean valid = true;
					boolean knowntype = true;
					
					String ext;
					int p = szfile.lastIndexOf('.');
					if (p == -1)
						ext = "";
					else {
						ext = szfile.substring(p + 1, szfile.length());
						ext = ext.toLowerCase();
					}
					
					if (ext.equalsIgnoreCase("tga"))
						valid = tga(response);
					else if (ext.equalsIgnoreCase("bmp"))
						valid = bmp(response);
					else if (ext.equalsIgnoreCase("gif"))
						valid = gif(response);
					else if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
						valid = jpg.decode(response);
					else if (ext.equalsIgnoreCase("png"))
						valid = png(response);
					else if (ext.equalsIgnoreCase("pcx"))
						valid = pcx(response);
					else if (ext.equalsIgnoreCase("psd"))
						valid = psd(response);
					else if (ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff"))
						valid = tiff(response);
					else
						knowntype = false; // "Unknown file extension."
					
					//
					read.close();
					
					//
					if (knowntype) {
						if (!valid)
							log_itemerror(iCORRUPT, "File format corrupt or wrong file extension.");
						
						//
						int iRow = query.addRow();
						query.setData(iRow, iFile, item.File);
						query.setData(iRow, iPath, item.Path);
						query.setData(iRow, iError, item.Error);
						query.setData(iRow, iType, item.Type);
						query.setData(iRow, iLastModified, item.LastModified);
						
						query.setData(iRow, iSize, String.valueOf(item.Size));
						
						query.setData(iRow, iWidth, String.valueOf(item.Width));
						query.setData(iRow, iHeight, String.valueOf(item.Height));
						query.setData(iRow, iPixels, String.valueOf(item.Pixels));
						
						query.setData(iRow, iPixelBits, String.valueOf(item.PixelBits));
						query.setData(iRow, iColors, String.valueOf((long) item.Colors)); //i64
						query.setData(iRow, iColorType, item.ColorType);
						query.setData(iRow, iCompression, item.Compression);
						query.setData(iRow, iDPI, String.valueOf(item.DPI));
						
						query.setData(iRow, iComments, item.Comments);
						query.setData(iRow, iLayers, String.valueOf(item.Layers));
						query.setData(iRow, iDuration, String.valueOf(item.Duration));
						
						query.setData(iRow, iTransparencyIndex, String.valueOf(item.TransparencyIndex));
						query.setData(iRow, iTransparency, String.valueOf(item.Transparency));
						query.setData(iRow, iFormat, item.Format);
					}
				}
			}
		}
		catch (NullPointerException e) {
			log_error(iFILEDOESNOTEXIST, "File does not exist (" + szfile + ").");
		}
	}
	
	/**
	 *
	 *
	 *
	 * Given a bitsize it can return the number of colors the bits can address.
	 * For example, 24 bits it about 16.7 million.
	 *
	 * @parm bits Number of bits the color occupies (ie, 24).
	 * @return Number of colors the specified bits can represent.
	 **/
	private long bitsToColors(int bits) {
		if(bits>32)
			return 0;
		else {
			long colors = 2L;
			int c = bits - 1;
			while (c-- > 0)
				colors *= 2L;
			return colors;
		}
	}
	
	/**
	 *
	 *
	 *
	 * BMP
	 *
	 * Recoded 11/29/99 because the way I did it the first time bugged me.
	 * Recoded 7/30/2000 for os/2 support
	 * recoded nov 2003 for java
	 *
	 * BMP - Microsoft Windows bitmap image file
	 * Byte Order: Little-endian
	 *
	 * Offset   Length        Contents
	 * 0          2 bytes  "BM"
	 * 2          4 bytes  Total size included "BM" magic (s)
	 * 6          2 bytes  Reserved1
	 * 8          2 bytes  Reserved2
	 * 10          4 bytes  Offset bits
	 * 14          4 bytes  Header size (n)
	 * 18        n-4 bytes  Header (See bellow)
	 * 14+n .. s-1           Image data
	 *
	 * Header: n==12 (Old BMP image file format, Used OS/2)
	 * Offset         Length   Contents
	 * 18                2 bytes  Width
	 * 20                2 bytes  Height
	 * 22                2 bytes  Planes
	 * 24                2 bytes  Bits per Pixel
	 *
	 * Header: n>12 (Microsoft Windows BMP image file)
	 * Offset   Length        Contents
	 * 18          4 bytes  Width
	 * 22          4 bytes  Height
	 * 26          2 bytes  Planes
	 * 28          2 bytes  Bits per Pixel
	 * 30          4 bytes  Compression
	 * 34          4 bytes  Image size
	 * 38          4 bytes  X Pixels per meter
	 * 42          4 bytes  Y Pixels per meter
	 * 46          4 bytes  Number of Colors
	 * 50          4 bytes  Colors Important
	 * 54 (n-40) bytes  OS/2 new extentional fields??
	 */
	@SuppressWarnings("unused")
	private boolean bmp(Response response) {
		item.Type = "BMP";
		
		class BMPH {
			long fileSize; // size of file in bytes
			long bfoffbits; // byte offset where image begins
			long bisize; // size of this header, 40 bytes
			long width; // image width in pixels
			long height; // image height in pixels
			int planes; // number of image planes (always 1)
			int bitsperpixel; // bits per pixel, 1,4,8, 24 or 32
			int compression; // compression type
			long compressedSize; // size of compressed image
			long xppm; // x pixels per meter
			long yppm; // y pixels per meter
			long colorsUsed; // number of colors in map
			long colorsImportant; // number of important colors
			byte[] colormap; //[256*4];	// pointer to color map, 4*N bytes
		}
		
		try {
			BMPH bmph = new BMPH();
			bmph.colormap = new byte[256 * 4];
			
			// read standard header
			char[] id = new char[2];
			id[0] = (char)read.BYTE();
			id[1] = (char)read.BYTE();
			
			String identifier = new String(id, (int) 0, (int) 2);
			
			int totalfilesize = (int) read.DWORD(); //filesize
			read.WORD(); //reserved 1
			read.WORD(); //reserved 2
			read.DWORD(); //offsetbits
			
			int headersize = (int) read.DWORD();
			
			//
			if (
					identifier.equals("BM") ||
					identifier.equals("BA") ||
					identifier.equals("CI") ||
					identifier.equals("CP") ||
					identifier.equals("IC") ||
					identifier.equals("PT")) {
				//format
				if (identifier.equals("BM"))
					item.Format = "MS BitMaP";
				else if (identifier.equals("BA"))
					item.Format = "OS/2 Bitmap Array";
				else if (identifier.equals("CI"))
					item.Format = "OS/2 Color Icon";
				else if (identifier.equals("CP"))
					item.Format = "OS/2 Color Pointer";
				else if (identifier.equals("IC"))
					item.Format = "OS/2 Icon";
				else if (identifier.equals("PT"))
					item.Format = "OS/2 Pointer";
				
				// bitmap header size
				if (headersize == 0x28)
					item.Format += " Windows 3.1+";
				else if (headersize == 0xc)
					item.Format += " OS/2 1.x";
				else if (headersize == 0xf0)
					item.Format += " OS/2 2.x";
				else if (headersize > 0xf0) {
					log_itemerror(iCORRUPT, "Invalid header size (" + String.valueOf(headersize) + ").");
					return false;
				}
				
				// extended header (os/2 and old bmp format)
				if (headersize == 0xc) {
					//
					bmph.width = read.WORD();
					bmph.height = read.WORD();
					
					bmph.planes = read.WORD();
					bmph.bitsperpixel = read.WORD();
					
					//
					item.Width = (int) bmph.width;
					item.Height = (int) bmph.height;
					item.Pixels = item.Width * item.Height;
					item.PixelBits = bmph.bitsperpixel * bmph.planes;
				}
				
				// extended extended header (newer windows only format)
				else if (headersize > 0xc) {
					//
					bmph.width = read.DWORD();
					bmph.height = read.DWORD();
					
					bmph.planes = read.WORD();
					bmph.bitsperpixel = read.WORD();
					
					bmph.compression = (int) read.DWORD();
					read.DWORD(); //compressedsize
					
					bmph.xppm = read.DWORD(); //xppm
					bmph.yppm = read.DWORD(); //yppm
					
					bmph.colorsUsed = read.DWORD();
					bmph.colorsImportant = read.DWORD();
					
					read.POINTER(); //colormap
					
					//
					item.Width = (int) bmph.width;
					item.Height = (int) bmph.height;
					item.Pixels = item.Width * item.Height;
					item.PixelBits = bmph.bitsperpixel * bmph.planes;
					
					item.Colors = bmph.colorsUsed;
					
					if (bmph.colorsUsed == 0)
						item.Colors = (int) Math.pow((double) 2, (double) item.PixelBits);
					
					switch (bmph.compression) {
					case 0:
						item.Compression = "None";
						break;
					case 2:
						item.Compression = "RLE";
						break;
					default:
						item.Compression += String.valueOf(bmph.compression);
					}
					
					switch (bmph.bitsperpixel) {
					case 1:
						item.ColorType = "FAX";
						break;
					case 4:
						item.ColorType = "CLUT16";
						break;
					case 8:
						item.ColorType = "CLUT";
						break;
					case 16:
						item.ColorType = "RGB555";
						break;
					case 24:
						item.ColorType = "RGB";
						break;
					case 32:
						item.ColorType = "RGB padded";
						break;
					}
					
					item.DPI = (int) Math.ceil(((double) bmph.xppm / 100.0) * (double) 2.54);
				}
			}
			else {
				log_itemerror(iCORRUPT, "Invalid header signature.");
				return false;
			}
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * PCX
	 * Added .9 11/30/1999
	 *
	 * Byte                Item                  Size         Description/Comments
	 * 0                        Manufacturer 1                Constant Flag, 10 = ZSoft .pcx
	 * 1                        Version          1                Version information
	 * 0        = Version 2.5 of PC        Paintbrush
	 * 2        = Version 2.8 w/palette        information
	 * 3        = Version 2.8 w/o palette information
	 * 4        = PC Paintbrush        for        Windows(Plus for
	 * Windows uses Ver 5)
	 * 5        = Version 3.0 and >        of PC Paintbrush
	 * and PC Paintbrush        +, includes
	 * Publisher's Paintbrush . Includes
	 * 24-bit .PCX files
	 * 2                        Encoding          1         1 = .PCX run length encoding
	 * 3                        BitsPerPixel  1         Number of bits to represent a pixel
	 * (per Plane) -        1, 2, 4, or        8
	 * 4                        Window                  8         Image Dimensions: Xmin,Ymin,Xmax,Ymax
	 * 12                 HDpi                   2         Horizontal Resolution of image in DPI*
	 * 14                 VDpi                   2         Vertical Resolution of image in DPI*
	 * 16                 Colormap          48         Color palette setting, see text
	 * 64                 Reserved           1         Should be set to 0.
	 * 65                 NPlanes           1         Number of color planes
	 * 66                 BytesPerLine  2         Number of bytes to allocate for a scanline
	 * plane.  MUST be an EVEN number.  Do NOT
	 * calculate        from Xmax-Xmin.
	 * 68                 PaletteInfo   2         How to interpret palette- 1 = Color/BW,
	 * 2        = Grayscale        (ignored in        PB IV/ IV +)
	 * 70                 HscreenSize   2         Horizontal screen size in pixels. New field
	 * found        only in        PB IV/IV Plus
	 * 72                 VscreenSize   2         Vertical screen size in pixels. New field
	 * found        only in        PB IV/IV Plus
	 * 74                 Filler           54         Blank to fill out 128 byte header.  Set all
	 * bytes        to 0
	 */
	@SuppressWarnings("unused")
	private boolean pcx(Response response) {
		item.Type = "PCX";
		
		try {
			//
			int manufacturer = read.BYTE();
			int version = read.BYTE();
			int encoding = read.BYTE();
			int bitsperpixel = read.BYTE();
			int xmin = read.WORD();
			int ymin = read.WORD();
			int xmax = read.WORD();
			int ymax = read.WORD();
			int width = xmax - xmin + 1;
			int height = ymax - ymin + 1;
			int hdpi = read.WORD();
			int vdpi = read.WORD();
			
			//
			int dpi = hdpi;
			
			byte[] colormap = read.DATA(48);
			
			read.BYTE();
			
			int nplanes = read.BYTE();
			int bytesperline = read.WORD();
			int paletteinfo = read.WORD();
			int hscreensize = read.WORD();
			int vscreensize = read.WORD();
			
			//
			if (read.isEOF()) {
				log_itemerror(iCORRUPT, "Header length short.");
				return false;
			}
			
			//
			byte[] filler = read.DATA(54);
			
			//
			if (encoding == 1)
				item.Compression += "PCX Run-length encoding. ";
			
			if (manufacturer == 10)
				item.Compression += "ZSoft. ";
			
			switch (version) {
			case 0:
				item.Format += "Version 2.5 of PC Paintbrush. ";
				break;
			case 2:
				item.Format += "Version 2.8 w/ palette information. ";
				break;
			case 3:
				item.Format += "Version 2.8 w/o palette information. ";
				break;
			case 4:
				item.Format += "PC Paintbrush for Windows. ";
				break;
			case 5:
				item.Format += "Version 3.0+ PC Paintbrush. ";
				break;
			}
			
			item.Width = width;
			item.Height = height;
			item.Pixels = width * height;
			item.PixelBits = bitsperpixel * nplanes;
			item.Colors = (int) Math.pow(2, bitsperpixel * nplanes);
			item.DPI = dpi;
			
			//
			if (item.PixelBits > 8)
				item.ColorType = "RGB";
			else {
				if (paletteinfo == 2)
					item.ColorType = "Grayscale";
				else
					item.ColorType = "CLUT";
			}
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * PSD
	 * Supports only ps 2.0 currently.
	 *
	 * 8BPS Photoshop 2
	 * 8BIM Photoshop 3/4
	 */
	@SuppressWarnings("unused")
	private boolean psd(Response response) {
		item.Type = "PSD";
		
		try {
			//
			String sig = read.STRING(4);
			if (sig.equalsIgnoreCase("8BPS"))
				;
			else if (sig.length() != 4) {
				log_itemerror(iCORRUPT, "Signature length short.");
				return false;
			}
			else {
				log_itemerror(iUNSUPPORTEDFORMAT, "Not Photoshop 2.0 8BPS.");
				return false;
			}
			
			//
			item.Format = "Photoshop 2.0 8BPS";
			
			//
			int version = read.WORD_BIGENDIAN();
			
			read.skip(6);
			
			int channels = read.WORD_BIGENDIAN();
			
			long rows = read.DWORD_BIGENDIAN();
			long columns = read.DWORD_BIGENDIAN();
			item.Width = (int) columns;
			item.Height = (int) rows;
			item.Pixels = (int) (rows * columns);
			
			int depth = read.WORD_BIGENDIAN();
			item.PixelBits = (int) (depth * channels);
			item.Colors = bitsToColors(depth * channels);
			
			int mode = read.WORD_BIGENDIAN();
			switch (mode) {
			case 0:
				item.ColorType = "Bitmap";
				break;
			case 1:
				item.ColorType = "Grayscale";
				break;
			case 2:
				item.ColorType = "CLUT";
				break;
			case 3:
				item.ColorType = "RGB";
				break;
			case 4:
				item.ColorType = "CMYK";
				break;
			case 7:
				item.ColorType = "Multichannel";
				break;
			case 8:
				item.ColorType = "Duotone";
				break;
			case 9:
				item.ColorType = "Lab Color";
				break;
			}
			
			long modedata_length = read.DWORD_BIGENDIAN();
			read.skip((int) modedata_length);
			
			long imageresources_length = read.DWORD_BIGENDIAN();
			read.skip((int) imageresources_length);
			
			long reserveddata_length = read.DWORD_BIGENDIAN();
			read.skip((int) reserveddata_length);
			
			int compression = read.WORD_BIGENDIAN();
			switch (compression) {
			case 0:
				item.Compression = "Raw Data";
				break;
			case 1:
				item.Compression = "RLE";
				break;
			}
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * TGA
	 * Truevision
	 */
	@SuppressWarnings("unused")
	private boolean tga(Response response) {
		item.Type = "TGA";
		
		//image	types
		final int TGA_Null = 0;
		final int TGA_Map = 1;
		final int TGA_RGB = 2;
		final int TGA_Mono = 3;
		final int TGA_RLEMap = 9;
		final int TGA_RLERGB = 10;
		final int TGA_RLEMono = 11;
		final int TGA_CompMap = 32;
		final int TGA_CompMap4 = 33;
		
		//interleave flag
		final int TGA_IL_None = 0;
		final int TGA_IL_Two = 1;
		final int TGA_IL_Four = 2;
		
		/**
		 **/
		class TGAH {
			short CharacterIdentificationField; //ubyte
			short ColorMapType; //ubyte
			short ImageTypeCode; //ubyte
			int ColorMapOrigin; //short int
			int ColorMapLength; //short int
			short ColorMapEntrySize; //ubyte
			int XOrigin; //short int
			int YOrigin; //short int
			int Width; //short int
			int Height; //short int
			short ImagePixelSize; //ubyte
			short ImageDescriptorByte; //ubyte
		}
		
		try {
			TGAH tgah = new TGAH();
			
			//
			tgah.CharacterIdentificationField = read.BYTE();
			tgah.ColorMapType = read.BYTE();
			tgah.ImageTypeCode = read.BYTE();
			
			tgah.ColorMapOrigin = read.WORD();
			tgah.ColorMapLength = read.WORD();
			tgah.ColorMapEntrySize = read.BYTE();
			
			tgah.XOrigin = read.WORD();
			tgah.YOrigin = read.WORD();
			
			tgah.Width = read.WORD();
			tgah.Height = read.WORD();
			
			tgah.ImagePixelSize = (short) read.BYTE();
			tgah.ImageDescriptorByte = (short) read.BYTE();
			
			//
			String CIF;
			if (tgah.CharacterIdentificationField != 2)
				CIF = read.STRING(tgah.CharacterIdentificationField);
			else
				CIF = "";
			
			//
			item.Width = tgah.Width;
			item.Height = tgah.Height;
			item.Pixels = tgah.Width * tgah.Height;
			item.PixelBits = tgah.ImagePixelSize;
			item.Comments = CIF;
			item.Colors = (long) Math.pow(2, tgah.ImagePixelSize);
			
			switch (tgah.ImageTypeCode) {
			case TGA_Null:
				item.Compression = "";
				break;
			case TGA_Map:
				item.Compression = "Uncompressed";
				break;
			case TGA_RGB:
				item.Compression = "Uncompressed";
				break;
			case TGA_Mono:
				item.Compression = "Uncompressed";
				break;
			case TGA_RLEMap:
				item.Compression = "RLE";
				break;
			case TGA_RLERGB:
				item.Compression = "RLE";
				break;
			case TGA_RLEMono:
				item.Compression = "RLE";
				break;
			case TGA_CompMap:
				item.Compression = "CompMap";
				break;
			case TGA_CompMap4:
				item.Compression = "CompMap4";
				break;
			}
			
			switch (tgah.ImageTypeCode) {
			case TGA_Null:
				item.ColorType = "";
				break;
			case TGA_Map:
				item.ColorType = "CLUT";
				break;
			case TGA_RGB:
				item.ColorType = "RGB";
				break;
			case TGA_Mono:
				item.ColorType = "Grayscale";
				break;
			case TGA_RLEMap:
				item.ColorType = "CLUT";
				break;
			case TGA_RLERGB:
				item.ColorType = "RGB";
				break;
			case TGA_RLEMono:
				item.ColorType = "Grayscale";
				break;
			case TGA_CompMap:
				item.ColorType = "CLUT";
				break;
			case TGA_CompMap4:
				item.ColorType = "CLUT";
				break;
			}
			
			//NEW TGA 2.0 Extensions?
			//int hold = read.getPosition();
			read.setPosition((int) (item.Size - 26));
			
			long ExtensionAreaOffset = read.DWORD();
			long DeveloperDirectoryOffset = read.DWORD();
			String signature = read.STRING(18);
			
			if (!signature.equals("TRUEVISION-XFILE."))
				item.Format = "TARGA 1.0";
			else {
				item.Format = "New TARGA 2.0";
				
				read.setPosition((int) ExtensionAreaOffset);
				
				int ExtLength = read.WORD();
				String Author = read.STRING(40);
				String Comments = read.STRING(324);
				
				if (Author.length() > 0)
					item.Comments = Author + Comments;
				else
					item.Comments = Comments;
			}
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * GIF
	 *
	 * gif87a an        gif89a
	 */
	@SuppressWarnings("unused")
	private boolean gif(Response response) {
		item.Type = "GIF";
		
		try {
			//
			String gifsig = read.STRING(6);
			if (!gifsig.equals("GIF87a") && !gifsig.equals("GIF89a")) {
				log_itemerror(iCORRUPT, "GIF87a/GIF89a header error.");
				return false;
			}
			
			//
			class PF {
				byte globalcolortable; //: 1;
				byte colorresolution; //: 3;
				byte sort; //: 1;
				byte sizeofglobalcolortable; //: 3;
			}
			PF pf = new PF();
			
			//
			item.Format = gifsig;
			
			//
			int ScreenWidth = read.WORD();
			int ScreenHeight = read.WORD();
			
			//
			short temp = (short) read.BYTE();
			pf.globalcolortable = (byte) ((temp<<7) & 1);
			pf.colorresolution = (byte) ((temp >>4) & 7);
			pf.sort = (byte) ((temp >> 3) & 1);
			pf.sizeofglobalcolortable = (byte) (temp & 7);
			
			//
			int BackgroundColor = (int) read.BYTE();
			int PixelAspectRatio = (int) read.BYTE();
			
			//
			int globalbytes = 3 * (int) (Math.pow(2, pf.sizeofglobalcolortable + 1));
			globalbytes = globalbytes >> ((3 - pf.colorresolution) * 2); //hack
			
			if (pf.globalcolortable > 0)
				read.skip(globalbytes);
			
			//
			item.Width = ScreenWidth;
			item.Height = ScreenHeight;
			item.Pixels = item.Width * item.Height;
			item.Colors = (int) (Math.pow(2, pf.sizeofglobalcolortable+1));
			item.PixelBits = pf.sizeofglobalcolortable+1;
			
			//
			short isc;
			int imageLeft;
			int imageTop;
			int imageWidth;
			int imageHeight;
			short mi000pixel;
			
			int layers = 0;
			long duration = 0;
			
			//
			do {
				//
				isc = read.BYTE();
				
				if (isc == ';') {
				}
				
				//
				if (isc == ',') {
					class IMAGEDESCRIPTOR {
						byte localcolortable; //: 1;
						byte interlace; //: 1;
						byte sort; //: 1;
						byte unused2; //: 1;
						byte unused3; //: 1;
						byte pixel; //: 3;
					}
					
					IMAGEDESCRIPTOR imagedescriptor = new IMAGEDESCRIPTOR();
					
					layers++;
					
					//
					imageLeft = read.WORD();
					imageTop = read.WORD();
					imageWidth = read.WORD();
					imageHeight = read.WORD();
					mi000pixel = (short) read.BYTE();
					
					//
					imagedescriptor.localcolortable = (byte) (mi000pixel & 1);
					imagedescriptor.interlace = (byte) ((mi000pixel >> 1) & 1);
					imagedescriptor.sort = (byte) ((mi000pixel >> 2) & 1);
					imagedescriptor.pixel = (byte) ((mi000pixel >> 5) & 7);
					
					if (imagedescriptor.localcolortable == 1) {
						int colorbytes = 3 * (int) (Math.pow(2, imagedescriptor.pixel + 1));
					}
					
					//
					int codesize = read.BYTE();
					int bytecount = read.BYTE();
					
					while (bytecount > 0) {
						read.skip(bytecount);
						bytecount = read.BYTE();
					}
				}
				
				//
				if (isc == '!') {
					int code = read.BYTE();
					
					int looping = 0;
					int loop = read.BYTE();
					
					while (loop > 0) {
						if ((code == 249) && (loop == 4)) {
							class R {
								byte Reserved; //: 			3;
								byte DisposalMethod; //:		3;
								byte UserInputFlag; //:		1;
								byte TransparentColorFlag; //: 1;
							}
							
							R r = new R();
							
							int rdut = read.BYTE();
							r.DisposalMethod = (byte) ((rdut >> 3) & 7);
							r.UserInputFlag = (byte) ((rdut >> 6) & 1);
							r.DisposalMethod = (byte) ((rdut >> 7) & 1);
							
							int delay = read.WORD();
							int transparentColorIndex = read.BYTE();
							
							item.TransparencyIndex = transparentColorIndex;
							
							duration += delay;
						}
						else if ((code == 255) && (loop == 3)) {
							read.BYTE();
							looping = read.WORD();
						}
						else
							read.skip(loop);
						
						loop = read.BYTE();
					}
				}
			}
			while ((isc == ',') || (isc == '!'));
			
			item.Layers = layers;
			item.Duration = duration * 10;
			
			//
			item.Compression = "LZW (Lempel-Ziv-Welch, Unisys US Patent No 4,558,302)";
			item.ColorType = "CLUT";
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * TIFF
	 *
	 * TIFF is monsterously complex (needlessly complex).
	 * This code        in no way is a complete        decoder, but it        gets at        the        most common        configs.
	 */
	@SuppressWarnings("unused")
	private boolean tiff(Response response) {
		item.Type = "TIFF";
		
		try {
			//
			boolean byteorder = false; //0=little endian (intel), 1=big endian (motorola)
			int bo0 = read.BYTE();
			int bo1 = read.BYTE();
			if ((bo0 == 'I') && (bo1 == 'I')) {
				byteorder = false; //intel
				item.Format = " Intel"; //little endian
			}
			else if ((bo0 == 'M') && (bo1 == 'M')) {
				byteorder = true; //motorola
				item.Format = " Motorola"; //big endian
			}
			else {
				log_itemerror(iUNSUPPORTEDFORMAT, "Not a known TIFF byte order.");
				return false;
			}
			
			//
			int tiffid;
			if (byteorder)
				tiffid = read.WORD_BIGENDIAN();
			else
				tiffid = read.WORD();
			if (tiffid != 42) {
				log_itemerror(iCORRUPT, "(!42) File does not have TIFF numeric signature.");
				return false;
			}
			
			//
			int ifd;
			if (byteorder)
				ifd = (int) read.DWORD_BIGENDIAN();
			else
				ifd = (int) read.DWORD();
			
			//
			read.setPosition(ifd);
			
			//
			long imageLength = 0;
			long imageWidth = 0;
			int bitspersample = 0;
			int samplesperpixel = 1;
			int resolutionUnit = 0;
			int xResolution = 0;
			int photometricInterpretation = 0;
			int colorMap = 0;
			
			byte[] buffer = null;
			int bufferlength = 0;
			
			//
			int tag = -1;
			int fieldtype;
			long count;
			long valueoffset;
			long valueoffset1;
			long valueoffset2;
			long valueoffset3;
			
			//
			int nde;
			if (byteorder)
				nde = read.WORD_BIGENDIAN();
			else
				nde = read.WORD();
			
			//
			long pos=0;
			while ((nde-- > 0) && !read.isEOF()) {
				//
				valueoffset = 0;
				valueoffset1 = 0;
				valueoffset2 = 0;
				valueoffset3 = 0;
				
				bufferlength = 0;
				
				pos = read.getPosition();
				
				//read ifd
				if (byteorder)
					tag = read.WORD_BIGENDIAN();
				else
					tag = read.WORD();
				
				if (byteorder)
					fieldtype = read.WORD_BIGENDIAN();
				else
					fieldtype = read.WORD();
				
				if (byteorder)
					count = read.DWORD_BIGENDIAN();
				else
					count = read.DWORD();
				
				//
				int fieldsize = 0;
				switch (fieldtype) {
				case 1:
					fieldsize = 1;
					break; //byte
				case 2:
					fieldsize = 1;
					break; //ascitem.
				case 3:
					fieldsize = 2;
					break; //short
				case 4:
					fieldsize = 4;
					break; //long
				case 5:
					fieldsize = 8;
					break; //rational
					//tiff 6.0 extensions
				case 6:
					fieldsize = 1;
					break; //sbyte
				case 7:
					fieldsize = 1;
					break; //undefined
				case 8:
					fieldsize = 2;
					break; //sshort
				case 9:
					fieldsize = 4;
					break; //slong
				case 10:
					fieldsize = 8;
					break; //srational
				case 11:
					fieldsize = 4;
					break; //float
				case 12:
					fieldsize = 8;
					break; //double
				}
				
				//
				long fc = count * fieldsize;
				if (fc <= 4) {
					//
					switch (fieldtype) {
					case 1:
					case 6:
					case 7:
						if (count == 0) {
						}
						else if (count == 1) {
							if (byteorder)
								valueoffset = read.BYTE_BIGENDIAN();
							else
								valueoffset = read.BYTE();
							
							read.BYTE();
							read.BYTE();
							read.BYTE();
						}
						else if (count == 2) {
							if (byteorder)
								valueoffset = read.BYTE_BIGENDIAN();
							else
								valueoffset = read.BYTE();
							
							if (byteorder)
								valueoffset1 = read.BYTE_BIGENDIAN();
							else
								valueoffset1 = read.BYTE();
							
							read.BYTE();
							read.BYTE();
						}
						else if (count == 3) {
							if (byteorder)
								valueoffset = read.BYTE_BIGENDIAN();
							else
								valueoffset = read.BYTE();
							
							if (byteorder)
								valueoffset1 = read.BYTE_BIGENDIAN();
							else
								valueoffset1 = read.BYTE();
							
							if (byteorder)
								valueoffset2 = read.BYTE_BIGENDIAN();
							else
								valueoffset2 = read.BYTE();
							
							read.BYTE();
						}
						else if (count == 4) {
							if (byteorder)
								valueoffset = read.BYTE_BIGENDIAN();
							else
								valueoffset = read.BYTE();
							
							if (byteorder)
								valueoffset1 = read.BYTE_BIGENDIAN();
							else
								valueoffset1 = read.BYTE();
							
							if (byteorder)
								valueoffset2 = read.BYTE_BIGENDIAN();
							else
								valueoffset2 = read.BYTE();
							
							if (byteorder)
								valueoffset3 = read.BYTE_BIGENDIAN();
							else
								valueoffset3 = read.BYTE();
						}
						break;
					case 2:
						if (byteorder)
							valueoffset = read.DWORD_BIGENDIAN();
						else
							valueoffset = read.DWORD();
						bufferlength = 8;
						buffer = new byte[bufferlength];
						
						//fix me!
						//buffer=valueoffset.substring(0,8);
						//strncpy((char *)buffer,(const char *)&valueoffset,bufferlength);
						break;
					case 3:
					case 8:
						if (count == 0) {
						}
						else if (count == 1) {
							if (byteorder)
								valueoffset = read.WORD_BIGENDIAN();
							else
								valueoffset = read.WORD();
							
							read.WORD();
						}
						else if (count == 2) {
							if (byteorder)
								valueoffset = read.WORD_BIGENDIAN();
							else
								valueoffset = read.WORD();
							
							if (byteorder)
								valueoffset2 = read.WORD_BIGENDIAN();
							else
								valueoffset2 = read.WORD();
						}
						break;
					case 4:
					case 9:
					case 11:
						if (count == 0) {
						}
						else if (count == 1) {
							if (byteorder)
								valueoffset = read.DWORD_BIGENDIAN();
							else
								valueoffset = read.DWORD();
						}
						break;
					case 5:
					case 10:
					case 12:
						break;
					}
				}
				else {
					//
					if (byteorder)
						valueoffset = read.DWORD_BIGENDIAN();
					else
						valueoffset = read.DWORD();
					
					pos = read.getPosition();
					read.setPosition((int) valueoffset);
					
					bufferlength = (int) (count * fieldsize);
					buffer = read.DATA(bufferlength);
					
					read.setPosition((int) pos);
				}
				
				// 259 compression SHORT
				if (tag == 259) {
					switch ((int) valueoffset) {
					case 1:
						item.Compression = "No compression";
						break;
					case 2:
						item.Compression = "CCIT Group 3 1-Dimensional Modified Huffman run length encoding";
						break;
					case 3:
						item.Compression = "T4-encoding: CCIT T.4 bi-level encoding";
						break;
					case 4:
						item.Compression = "T6-encoding: CCIT T.6 bi-level encoding";
						break;
					case 5:
						item.Compression = "LZW (Lempel-Ziv-Welch, Unisys US Patent No 4,558,302)";
						break;
					case 6:
						item.Compression = "JFIF(JPEG)";
						break;
					case 32773:
						item.Compression = "Packbits compression";
						break;
					default:
						item.Compression = "Type " + String.valueOf(valueoffset);
					}
				}
				
				// 257 height SHORT or LONG
				if (tag == 257)
					imageLength = valueoffset;
				
				//256 width SHORT or LONG
				if (tag == 256)
					imageWidth = valueoffset;
				
				// 258 bitspersample SHORT
				if (tag == 258) {
					bitspersample = 1;
					
					if (fc <= 4)
						bitspersample = (int) valueoffset;
					else {
						//
						int c = (int) count;
						int p = 0;
						
						bitspersample = 0;
						while (c-- > 0) {
							if (byteorder)
								bitspersample += (buffer[p + 1] + (buffer[p] << 8));
							else
								bitspersample += (buffer[p] + (buffer[p + 1] << 8));
							
							p += fieldsize;
						}
					}
				}
				
				// 277 samplesperpixel SHORT
				if (tag == 277)
					samplesperpixel = (int) valueoffset;
				
				//296 res unit (dots per ?)
				if (tag == 296)
					resolutionUnit = (int) valueoffset;
				
				//h dpi
				if (tag == 282) {
					// this converts the signed bytes into unsigned int
					// so we can create proper DWORD data from it.
					int[] u = new int[4];
					for (int n = 0; n < 4; n++)
						u[n] = (buffer[n] >= 0)        ? buffer[n] : buffer[n] + 128;
						//
						if (byteorder)
							xResolution = u[3] + (u[2] << 8) + (u[1] << 16) + (u[0] << 24);
						else
							xResolution = u[0] + (u[1] << 8) + (u[2] << 16) + (u[3] << 24);
				}
				
				//v dpi
				if (tag == 283)
					; //skip
				
				//photometricinterpretation
				if (tag == 262)
					photometricInterpretation = (int) valueoffset;
				
				//colormap
				if (tag == 320)
					colorMap = (int) valueoffset;
				
				// ascitem.
				if (fieldtype == 2) {
					switch (tag) {
					case 315:
						item.Comments += ("[ARTIST]" + new String(buffer) + "\n");
						break;
					case 33432:
						item.Comments += ("[COPYRIGHT]" + new String(buffer) + "\n");
						break;
					case 316:
						item.Comments += ("[HOSTCOMPUTER]" + new String(buffer) + "\n");
						break;
					case 270:
						item.Comments += ("[IMAGEDESCRIPTION]" + new String(buffer) + "\n");
						break;
					case 305:
						item.Comments += ("[SOFTWARE]" + new String(buffer) + "\n");
						break;
					}
				}
			}
			
			//
			item.Width = (int) imageWidth;
			item.Height = (int) imageLength;
			item.Pixels = item.Width * item.Height;
			item.PixelBits = bitspersample;
			
			//
			item.Colors = bitsToColors(bitspersample);
			
			//
			switch (photometricInterpretation) {
			case 0:
				item.ColorType = "WhiteIsZero";
				break;
			case 1:
				item.ColorType = "BlackIsZero";
				break;
			case 2:
				item.ColorType = "RGB";
				break;
			case 3:
				item.ColorType = "CLUT";
				break;
			case 4:
				item.ColorType = "Transparency mask";
				break;
			case 5:
				item.ColorType = "CMYK";
				break;
			case 6:
				item.ColorType = "YCbCr";
				break;
			case 7:
				item.ColorType = "CIELab";
				break;
			}
			
			//
			switch (resolutionUnit) {
			case 1: //undefined
				break;
			case 2: //inch
				item.DPI = xResolution;
				break;
			case 3: //centimeter
				item.DPI = (int) Math.ceil((double) xResolution * (double) 2.54);
				break;
			}
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	/**
	 *
	 *
	 *
	 * PNG
	 * Portable Network Graphics
	 *
	 */
	@SuppressWarnings("unused")
	private boolean png(Response response) {
		item.Type = "PNG";
		
		class PNGH {
			int width; //0-3
			int height; //4-7
			byte bit_depth; //8
			byte color_type; //9
			byte compression_method; //10
			byte filter_method; //11
			byte interlace_method; //12
		}
		
		try {
			PNGH pngh = new PNGH();
			int chunkLength = 0;
			String chunkType = "";
			int CRC = 0;
			
			// signature by spec: hex 89 50 4e 47 0d 0a 1a 0a
			//                    dec 128+9 80 78 71 13 10 26 10
			// sig using annoying java signed bytes: 8240 50 4e 47 0d 0a 1a 0a
			String pngsignature = read.STRING(8);
			if (pngsignature.length() != 8) {
				log_itemerror(iCORRUPT, "Signature length short.");
				return false;
			}
			else if (((int) pngsignature.charAt(0) != 8240) || ((int) pngsignature.charAt(1) != 80) || ((int) pngsignature.charAt(2) != 78) || ((int) pngsignature.charAt(3) != 71) || ((int) pngsignature.charAt(4) != 13) || ((int) pngsignature.charAt(5) != 10) || ((int) pngsignature.charAt(6) != 26) || ((int) pngsignature.charAt(7) != 10)) {
				log_itemerror(iCORRUPT, "Signature invalid (" + pngsignature + ").");
				return false;
			}
			else {
				//
				item.Format = "PNG";
				
				//
				try {
					while (!read.isEOF()) {
						chunkLength = (int) read.DWORD_BIGENDIAN();
						chunkType = read.STRING(4); //dWORD_BIGENDIAN();
						
						//simple checks for corrupt/invalid data
						if (chunkLength <= 0)
							break;
						else if (chunkType.length() != 4)
							break;
						
						//
						if (chunkType.equals("IEND"))
							break;
						
						else if (chunkType.equals("IHDR")) {
							byte[] buffer = null;
							buffer = new byte[chunkLength];
							buffer = read.DATA(chunkLength);
							if (buffer.length != chunkLength) {
								log_itemerror(iCORRUPT, "Chunk length overrun.");
								return false;
							}
							
							// this converts the signed bytes into unsigned int
							// so we can create proper DWORD data from it.
							int[] u = new int[8];
							for (int n = 0; n < 8; n++) {
								u[n] = (buffer[n] >= 0)        ? buffer[n] : buffer[n] + 128;
							}
							
							//
							pngh.width = (u[0] << 24) + (u[1] << 16) + (u[2] << 8) + u[3];
							pngh.height = (u[4] << 24) + (u[5] << 16) + (u[6] << 8) + u[7];
							pngh.bit_depth = buffer[8];
							pngh.color_type = buffer[9];
							pngh.compression_method = buffer[10];
							pngh.filter_method = buffer[11];
							pngh.interlace_method = buffer[12];
						}
						
						else if (chunkType.equals("tEXt")) {
							byte[] buffer = null;
							buffer = new byte[chunkLength];
							buffer = read.DATA(chunkLength);
							if (buffer.length != chunkLength) {
								log_itemerror(iCORRUPT, "Chunk length overrun.");
								return false;
							}
							//
							String sz1 = "";
							String sz2 = "";
							int i;
							for (i = 0; i < buffer.length; i++) {
								if (buffer[i] == 0)
									break;
								sz1 += (char) buffer[i];
							}
							for (; i < buffer.length; i++)
								sz2 += (char) buffer[i];
							item.Comments += ("[" + sz1 + "]" + sz2 + " ");
						}
						
						else
							read.skip(chunkLength);
						
						//
						CRC = (int) read.DWORD_BIGENDIAN();
						if (read.isEOF()) {
							log_itemerror(iCORRUPT, "No CRC.");
							return false;
						}
					}
				}
				catch (OutOfMemoryError e) {
					log_itemerror(iCORRUPT, "Memory failure.");
					return false;
				}
				catch(EOFException e) {
					log_itemerror(iCORRUPT,"Premature EOF.");
					return false;
				}
				catch (Exception e) {
					log_itemerror(iCORRUPT, "Data stream corrupted.");
					return false;
				}
				
				//
				item.Width = pngh.width;
				item.Height = pngh.height;
				item.Pixels = item.Width * item.Height;
				
				switch (pngh.color_type) {
				case 0: //grayscale
					item.PixelBits = pngh.bit_depth;
					item.ColorType = "Grayscale";
					break;
				case 2: //rgb
					item.PixelBits = pngh.bit_depth * 3;
					item.ColorType = "RGB";
					break;
				case 3: //clut
					item.PixelBits = pngh.bit_depth;
					item.ColorType = "CLUT";
					break;
				case 4: //ga (grayscale/alpha)
					item.PixelBits = pngh.bit_depth * 2;
					item.ColorType = "Grayscale/Alpha";
					break;
				case 6: //rgba
					item.PixelBits = pngh.bit_depth * 4;
					item.ColorType += "RGBA";
					break;
				}
				
				//
				item.Colors = bitsToColors(item.PixelBits);
				
				//
				switch (pngh.compression_method) {
				case 0:
					item.Compression = "Method 0 (32k sliding window deflate/inflate compression) ";
					break;
				default:
					break;
				}
			}
			
			return true;
		}
		catch(EOFException e) {
			log_itemerror(iCORRUPT,"Premature EOF.");
			return false;
		}
	}
	
	
	/**
	 *
	 *
	 *
	 * JPG
	 * (jpeg in jfif)
	 */
	@SuppressWarnings("unused")
	class JPG {
		private final int M_SOF0 = 0xC0; // Start Of Frame N
		private final int M_SOF1 = 0xC1; // N indicates which compression process
		private final int M_SOF2 = 0xC2; // Only SOF0-SOF2 are now in common use
		private final int M_SOF3 = 0xC3;
		private final int M_SOF5 = 0xC5; // NB: codes C4 and CC are NOT SOF markers
		private final int M_SOF6 = 0xC6;
		private final int M_SOF7 = 0xC7;
		private final int M_SOF9 = 0xC9;
		private final int M_SOF10 = 0xCA;
		private final int M_SOF11 = 0xCB;
		private final int M_SOF13 = 0xCD;
		private final int M_SOF14 = 0xCE;
		private final int M_SOF15 = 0xCF;
		private final int M_SOI = 0xD8; // Start Of Image (beginning of datastream)
		private final int M_EOI = 0xD9; // End Of Image (end of datastream)
		private final int M_SOS = 0xDA; // Start Of Scan (begins compressed data)
		private final int M_COM = 0xFE; // COMment
		private final int M_APP0 = 0xE0; // APP0
		
		//
		int sof = 0;
		int sofnmark = 0;
		
		/**
		 * main
		 **/
		private boolean decode(Response response) {
			item.Type = "JPEG";
			
			try {
				//
				int length;
				int count;
				sof = 0;
				
				do {
					//
					if ((length = jpg_sof()) == 0)
						break;
					if (sof == 0)
						break;
					
					//
					count = jpg_data(length);
					if (count == 0)
						break;
				}
				while ((sof != M_SOS) && (sof != M_EOI));
				
				//
				if (sofnmark == 0) {
					log_itemerror(iCORRUPT, "No SOFn header.");
					return false;
				}
				
				return true;
			}
			catch(EOFException e) {
				log_itemerror(iCORRUPT,"Premature EOF.");
				return false;
			}
		}
		/**
		 */
		int jpg_sof() throws EOFException {
			//
			int c1 = 0;
			
			//
			int c2 = 0;
			int length = 0;
			
			//
			while (!read.isEOF()) {
				//
				c1 = read.BYTE_BIGENDIAN();
				c2 = read.BYTE_BIGENDIAN();
				
				if (!read.isEOF()) {
					if (c1 == 0xFF)
						sof = c2;
					else {
						length = (c1 << 8) + c2;
						break;
					}
				}
			}
			
			//
			if (read.isEOF()) {
				sof = 0;
				length = 0;
			}
			
			return length;
		}
		/**
		 */
		int jpg_data(int length) throws EOFException {
			int count = 0;
			
			//
			if (length < 2)
				log_itemerror(iCORRUPT, "Erroneous JPEG marker length.");
			else {
				switch (sof) {
				case M_SOF0: // Baseline
				case M_SOF1: // Extended sequential, Huffman
				case M_SOF2: // Progressive, Huffman
				case M_SOF3: // Lossless, Huffman
				case M_SOF5: // Differential sequential, Huffman
				case M_SOF6: // Differential progressive, Huffman
				case M_SOF7: // Differential lossless, Huffman
				case M_SOF9: // Extended sequential, arithmetic
				case M_SOF10: // Progressive, arithmetic
				case M_SOF11: // Lossless, arithmetic
				case M_SOF13: // Differential sequential, arithmetic
				case M_SOF14: // Differential progressive, arithmetic
				case M_SOF15: // Differential lossless, arithmetic
					count = process_SOFn(sof, length);
					sofnmark = 1;
					break;
				case M_SOS:
					count = process_default(length);
					break;
				case M_EOI:
					count = process_default(length);
					break;
				case M_COM:
					count = process_COM(length);
					break;
				case M_APP0:
					count = process_App0(length);
					break;
				default:
					count = process_default(length);
				break;
				}
			}
			
			return count;
		}
		/**
		 */
		int process_SOFn(int marker, long length) throws EOFException {
			//
			int count = 0;
			
			//
			long image_height;
			long image_width;
			int data_precision;
			int num_components;
			
			int ci;
			
			try {
				//
				data_precision = read.BYTE_BIGENDIAN();
				image_height = read.WORD_BIGENDIAN();
				image_width = read.WORD_BIGENDIAN();
				num_components = read.BYTE_BIGENDIAN();
				
				//
				if (length != (8 + num_components * 3))
					log_itemerror(iCORRUPT, "Corrupt SOF marker length.");
				
				//
				for (ci = 0; ci < num_components; ci++, count += 3) {
					read.BYTE_BIGENDIAN();
					read.BYTE_BIGENDIAN();
					read.BYTE_BIGENDIAN();
				}
				
				//
				switch (marker) {
				case M_SOF0:
					item.Compression = "Baseline";
					break;
				case M_SOF1:
					item.Compression = "Extended sequential";
					break;
				case M_SOF2:
					item.Compression = "Progressive";
					break;
				case M_SOF3:
					item.Compression = "Lossless";
					break;
				case M_SOF5:
					item.Compression = "Differential sequential";
					break;
				case M_SOF6:
					item.Compression = "Differential progressive";
					break;
				case M_SOF7:
					item.Compression = "Differential lossless";
					break;
				case M_SOF9:
					item.Compression = "Extended sequential, arithmetic coding";
					break;
				case M_SOF10:
					item.Compression = "Progressive, arithmetic coding";
					break;
				case M_SOF11:
					item.Compression = "Lossless, arithmetic coding";
					break;
				case M_SOF13:
					item.Compression = "Differential sequential, arithmetic coding";
					break;
				case M_SOF14:
					item.Compression = "Differential progressive, arithmetic coding";
					break;
				case M_SOF15:
					item.Compression = "Differential lossless, arithmetic coding";
					break;
				default:
					item.Compression = "Unknown";
				break;
				}
				
				//
				item.Width = (int) image_width;
				item.Height = (int) image_height;
				item.Pixels = (int) (item.Width * item.Height);
				item.PixelBits = data_precision * num_components;
				
				//
				item.Colors = bitsToColors(item.PixelBits);
				
				//
				if (num_components == 1)
					item.ColorType = "Grayscale";
				else if (num_components == 3)
					item.ColorType = "RGB";
				else if (num_components == 4)
					item.ColorType = "CMYK";
				else if (num_components == 2)
					item.ColorType = "??";
				
				//
				item.Format = "JFIF/JPEG";
				
				//
				return count;
			}
			catch(EOFException e) {
				throw e;
			}
		}
		/**
		 */
		int process_COM(int length) throws EOFException {
			try {
				item.Comments = read.STRING(length - 2);
			}
			catch(EOFException e) {
				throw e;
			}
			return item.Comments.length();
		}
		/**
		 */
		int process_App0(int length) throws EOFException {
			int version;
			short density_unit;
			int X_density;
			int Y_density;
			short xThumbnail;
			short yThumbnail;
			
			try {
				//
				byte[] identifier = read.DATA(5);
				
				//
				version = read.WORD_BIGENDIAN();
				density_unit = (short) read.BYTE_BIGENDIAN();
				X_density = read.WORD_BIGENDIAN();
				Y_density = read.WORD_BIGENDIAN();
				xThumbnail = (short) read.BYTE_BIGENDIAN();
				yThumbnail = (short) read.BYTE_BIGENDIAN();
				
				//
				int len = length - 2 - 14;
				
				if (len > 0)
					read.skip(len);
				
				//
				if (!read.isEOF()) {
					switch (density_unit) {
					case 0: //undefined
						break;
					case 1: //inch
						item.DPI = X_density;
						break;
					case 2: //centimeter
						item.DPI = (int) Math.ceil((double) X_density * (double) 2.54);
						break;
					}
				}
				
				return length - 2;
				
			}
			catch(EOFException e) {
				throw e;
			}
			
		}
		/**
		 */
		@SuppressWarnings("finally")
		int process_default(int length) throws EOFException {
			String buffer=null;
			try {
				buffer = read.STRING(length - 2);
			}
			catch(EOFException e) {
				throw e;
			}
			finally {
				return buffer.length();
			}
		}
	}
	
	/**
	 *
	 *
	 *
	 * the READ class is a set of functions used to read in data types in a
	 * simple uniform manner.
	 * @author Lewis A. Sellers (min)
	 */
	class READ {
		private FileInputStream fis = null;
		private int iFPosition = 0;
		private String lastFilename = "";
		private boolean EOF = false;
		
		/**
		 * opens file by name of filename and readies it for reading.
		 */
		boolean open(String filename) {
			lastFilename = filename;
			iFPosition = 0;
			EOF = false;
			
			try {
				fis = new FileInputStream(filename);
			}
			catch (FileNotFoundException e) {
				log_itemerror(iFILEDOESNOTEXIST, "File does not exist (" + filename + ").");
				EOF = true;
			}
			catch (SecurityException e) {
				log_itemerror(iSECURITY, "Security on file (" + filename + ").");
				EOF = true;
			}
			
			return !EOF; //true if file opens, false if can not be
		}
		
		/**
		 * closes current read file.
		 */
		boolean close() {
			EOF = false;
			
			try {
				fis.close();
			}
			catch (IOException e) {
				log_itemerror(iFILEDOESNOTEXIST, "File not closed properly.");
			}
			
			return !EOF; //1 if ok, 0 if error
		}
		
		/**
		 *gets the current read position in the open file.
		 */
		int getPosition() {
			return iFPosition;
		}
		
		/**
		 *sets the position into the current open file.
		 */
		void setPosition(int pos) throws EOFException {
			close();
			open(lastFilename);
			skip(pos);
		}
		
		/**
		 *checks if end of file has been reached.
		 *returns true if it has.
		 */
		boolean isEOF() {
			return EOF;
		}
		
		/**
		 * the fundamental BYTE reading method that all other methods use
		 * to read data.
		 * reads in one byte at a time from bufered input
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns last byte read as an unsigned byte in char
		 */
		short NEXTBYTE() throws EOFException {
			short ch;
			
			if (EOF)
				throw new EOFException();
			else {
				try {
					ch = (short) fis.read();
					if (ch < 0)
						ch += 128;
					
					iFPosition++;
				}
				catch (IOException e) {
					EOF = true;
					throw new EOFException();
				}
			}
			
			return ch;
		}
		
		/**
		 * reads up to len bytes of data and returns as a java byte[] array.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @parm len number of bytes to read in
		 * @returns byte[] array with the data that was read in
		 */
		byte[] DATA(int len) throws EOFException {
			if (len <= 0)
				return null;
			
			int slen;
			byte[] buffer;
			
			try {
				buffer = new byte[len];
				slen = fis.read(buffer, 0, len);
				iFPosition += slen;
			}
			catch (IOException e) {
				EOF = true;
				buffer = null;
			}
			
			if (EOF)
				throw new EOFException();
			
			return buffer;
		}
		
		/**
		 * reads in a null terminated ascii string and converts it to a java String type.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns the ascii data read as a String type
		 */
		String STRING() throws EOFException {
			String str = "";
			short ch = 0;
			
			try {
				do {
					ch = NEXTBYTE();
					iFPosition++;
					str += (char) ch;
				}
				while (ch != 0);
			}
			catch (EOFException e) {
				EOF = true;
			}
			
			if (EOF)
				throw new EOFException();
			
			return str;
		}
		
		/**
		 * reads up to len bytes of ascii data and converts it to a java String type.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @parm len number of bytes to read in as a string
		 * @returns the ascii data read as a String type
		 */
		String STRING(int len) throws EOFException {
			if (len <= 0)
				return "";
			
			int slen;
			String buffer;
			
			try {
				byte[] bytebuffer = new byte[len];
				slen = fis.read(bytebuffer, 0, len);
				
				iFPosition += slen;
				buffer = new String(bytebuffer);
			}
			catch (IOException e) {
				buffer = "";
				EOF = true;
			}
			
			if (EOF)
				throw new EOFException();
			
			return buffer;
		}
		
		/**
		 * skips n bytes further into current open file.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @parm len number of bytes to skip
		 * @returns integer indicating number of bytes actually skipped
		 */
		int skip(int len) throws EOFException {
			if (len <= 0)
				return 0;
			
			long slen;
			
			try {
				slen = fis.skip((long) len);
				iFPosition += slen;
			}
			catch (IOException e) {
				EOF = true;
				slen = 0;
			}
			
			if (EOF)
				throw new EOFException();
			
			return (int) slen;
		}
		
		/**
		 * reads a big endian (motorola) byte from file stream
		 * and returns a 16-bit char.
		 * same as BYTE method.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 8-bit unsigned byte as a short
		 */
		short BYTE_BIGENDIAN() throws EOFException {
			return (short) NEXTBYTE();
		}
		
		/**
		 * reads a single unsigned byte from the file stream
		 * and passes back a 16-bit char.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 8-bit unsigned byte as a short
		 */
		short BYTE() throws EOFException {
			return (short) NEXTBYTE();
		}
		
		/**
		 * reads a normal unsigned WORD from the file stream
		 * and passes back a java signed integer.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 16-bit word as an integer
		 */
		int WORD() throws EOFException {
			int c1;
			int c2;
			c1 = NEXTBYTE();
			c2 = NEXTBYTE();
			
			return ((int) c2 << 8) + (int) c1;
		}
		
		/**
		 * reads a normal unsigned bigendian WORD from the file stream
		 * and passes back a java signed integer.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 16-bit bigendian word as an integer
		 */
		int WORD_BIGENDIAN() throws EOFException {
			int c1;
			int c2;
			c1 = NEXTBYTE();
			c2 = NEXTBYTE();
			
			return ((int) c1 << 8) + (int) c2;
		}
		
		/**
		 * reads a normal unsigned DWORD from the file stream
		 * and passes it back as a java signed long.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 32-bit dword as a long
		 */
		long DWORD() throws EOFException {
			long c1;
			long c2;
			long c3;
			long c4;
			c1 = NEXTBYTE();
			c2 = NEXTBYTE();
			c3 = NEXTBYTE();
			c4 = NEXTBYTE();
			
			return ((long) c4 << 24) + ((long) c3 << 16) + ((long) c2 << 8) + (long) c1;
		}
		
		/**
		 * reads a normal unsigned bigendian DWORD from the file stream
		 * and passes it back as a java signed long.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 32-bit bigendian dword as a long
		 */
		long DWORD_BIGENDIAN() throws EOFException {
			long c1;
			long c2;
			long c3;
			long c4;
			c1 = NEXTBYTE();
			c2 = NEXTBYTE();
			c3 = NEXTBYTE();
			c4 = NEXTBYTE();
			
			return ((long) c1 << 24) + ((long) c2 << 16) + ((long) c3 << 8) + (long) c4;
		}
		
		/**
		 * reads a DWORD pointer from the file stream.
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 32-bit pointer as a long
		 */
		long POINTER() throws EOFException {
			long c1;
			long c2;
			long c3;
			long c4;
			c1 = NEXTBYTE();
			c2 = NEXTBYTE();
			c3 = NEXTBYTE();
			c4 = NEXTBYTE();
			
			return ((long) c4 << 24) + ((long) c3 << 16) + ((long) c2 << 8) + (long) c1;
		}
		
		/**
		 * SYNCSAFE INTEGER
		 * for reading 7-bit bigendian dwords typicalled used in stream syncing w/ mp3
		 * @author Lewis A. Sellers (min)
		 * @exception EOFException
		 * @returns 32-bit syncsafe integer as a long
		 */
		long SYNCSAFE_INTEGER() throws EOFException {
			long c1;
			long c2;
			long c3;
			long c4;
			c1 = read.BYTE();
			c2 = read.BYTE();
			c3 = read.BYTE();
			c4 = read.BYTE();
			
			return ((c1 & 127) << 21) + ((c2 & 127) << 14) + ((c3 & 127) << 7) + c4;
		}
	}
	
	/**
	 * Our exception class to handle premature eond of file conditions.
	 * This is passed up from the read class to the calling methods.
	 * Essentially this is just a renamed IOException that occurs under known
	 * cirumstances.
	 * @author Lewis A. Sellers (min)
	 * @returns nothing
	 **/
	@SuppressWarnings("serial")
	class EOFException extends Exception {
		EOFException() {
			super();
			//
		}
		EOFException(String errMsg) {
			super(errMsg);
			//
		}
	}
	
	
	/** Error string for logging missing files or files unaccessable because of file security. */
	String strError = "";
	
	/**
	 * Numeric error types.
	 **/
	final int iFILEDOESNOTEXIST = 0;
	final int iSECURITY = 1;
	final int iCORRUPT = 2;
	final int iGRAMMAR = 3;
	final int iUNSUPPORTEDFORMAT = 4;
	final int iUNSUPPORTEDSUBFORMAT = 5;
	final int iENDOFFILE = 7;
	
	/**
	 *
	 *
	 *
	 * Logs general errors which happen before decoding of a specific file occurs.
	 * In general, this is used only to report iFILEDOESNOTEXIST or iSECURITY
	 * type errors.
	 *
	 * The error is returned in the ColdFusion error variable, separate from
	 * the returned query data itself.
	 *
	 * @parm err Numeric error type code such as iFILEDOESNOTEXIST, etc.
	 * @parm error Text string giving detailed information on the error.
	 * @return void
	 */
	private void log_error(int err, String error) {
		switch (err) {
		case iFILEDOESNOTEXIST:
			strError += ("[FILEDOESNOTEXIST]" + error);
			break;
		case iSECURITY:
			strError += ("[SECURITY]" + error);
			break;
		case iCORRUPT:
			strError += ("[CORRUPT]" + error);
			break;
		case iGRAMMAR:
			strError += ("[GRAMMAR]" + error);
			break;
		case iUNSUPPORTEDFORMAT:
			strError += ("[UNSUPPORTEDFORMAT]" + error);
			break;
		case iUNSUPPORTEDSUBFORMAT:
			strError += ("[UNSUPPORTEDSUBFORMAT]" + error);
			break;
		case iENDOFFILE:
			strError += ("[ENDOFFILE]" + error);
			break;
		}
	}
	
	/**
	 *
	 *
	 *
	 * Logs specific errors that occur during the decoding of a file.
	 * In general, this is will never report iFILEDOESNOTEXIST or iSECURITY
	 * type errors, but may return any other known error type.
	 *
	 * The error is returned as part of the query data in the "Error" field.
	 *
	 * @parm err Numeric error type code such as iCORRUPT, etc.
	 * @parm error Text string giving detailed information on the error.
	 * @return void
	 **/
	private void log_itemerror(int err, String error) {
		switch (err) {
		case iFILEDOESNOTEXIST:
			item.Error += ("[FILEDOESNOTEXIST]" + error);
			break;
		case iSECURITY:
			item.Error += ("[SECURITY]" + error);
			break;
		case iCORRUPT:
			item.Error += ("[CORRUPT]" + error);
			break;
		case iGRAMMAR:
			item.Error += ("[GRAMMAR]" + error);
			break;
		case iUNSUPPORTEDFORMAT:
			item.Error += ("[UNSUPPORTEDFORMAT]" + error);
			break;
		case iUNSUPPORTEDSUBFORMAT:
			item.Error += ("[UNSUPPORTEDSUBFORMAT]" + error);
			break;
		case iENDOFFILE:
			item.Error += ("[ENDOFFILE]" + error);
			break;
		}
	}
	
}
