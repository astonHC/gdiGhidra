/* Copyright (C) Harry Clark */

/* SEGA Dreamcast GDI Tool for GHIDRA */

/* THIS FILE PERTAINS TO THE FUNCTIONALITY OF LOADING THE INNATE */
/* CONTENTS OF THE GDI ROM RESPECTIVELY */

/* THIS IS BY INTIALISING THE BYTEWISE VALUE OF THE IRQ MASKS */
/* TO CHECK FOR THE ROM WHEN PUT INTO THE CONSOLE USING THE */
/* VECTOR TABLE */

/* SEE SEGA DREAMCAST HARDWARE SPECIFICATION SECTION 6: */
/* https://segaretro.org/images/8/8b/Dreamcast_Hardware_Specification_Outline.pdf#page=35 */

package main.dc;

/* NESTED INCLUDES */

import java.io.*;
import java.util.*;

/* GHIDRA INCLUDES */

import ghidra.app.util.Option;
import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractLibrarySupportLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.app.util.opinion.Loader;
import ghidra.framework.model.DomainObject;
import ghidra.program.flatapi.FlatProgramAPI;
import ghidra.program.model.address.Address;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.SourceType;
import ghidra.util.exception.CancelledException;
import ghidra.util.exception.InvalidInputException;
import ghidra.util.task.TaskMonitor;

public class DC_Loader
{
    /* SEEK VALUES FOR VECTOR TABLE HEADER CHECKSUM */

    public static int SEEK_SET = 0;
    public static int SEEK_CUR = 1;
    public static int SEEK_END = 2;

    /* DE FACTO STANDARD HEX VALUES FOR CD-ROMS  */

    public static long DC_BASE = 0x20000000;
    public static long DC_INIT = 0x80000000;
    public static final long DC_BASE_ADDR = DC_BASE + 0x1000;
    public static String DC_LOADER = "DREAMCAST GDI LOADER";
    public static String DC_ID = "HKIT 3030";

    /* RETURN THE NAME OF THE PLUGIN LOADER */

    public static String GET_BASE_NAME()
    {
        return DC_LOADER;
    }

    /* LOCALLY DECLARED CONSTRUCTOR FOR READING THE CONTENTS OF THE HEADER */

    public GDI(BinaryReader READER)
    {
        this.READ_HEADER(READER);
    }

    /* READS THE CONTENTS OF THE HEADER */
    /* THIS IS ASSUMING THE ARBITARY CASES ARE IN PLACE SUCH AS TEXT AND DATA */

    /* THIS FUNCTION WILL LOOK OVER THE OFFSETS, MEMORY ADDRESSES, AND ARBITARY SIZE OF */
    /* EACH RESPECTIVE SECTION */

    private static void READ_HEADER(BinaryReader READER, GDI GDI)
    {
        try 
        {            
            READER.setPointerIndex(0);

            for (int i = 0; i < 7; i++)
            {
               GDI.OFFSETS.TEXT_OFFSET[i] += READER.readNextUnsignedInt();
               GDI.OFFSETS.TEXT_MEM_ADDR[i] += READER.readNextUnsignedInt();
               GDI.OFFSETS.TEXT_SIZE[i] += READER.readNextUnsignedInt();
            }

            for (int j = 0; j < 11; j++)
            {
                GDI.OFFSETS.DATA_OFFSET[j] += READER.readNextUnsignedInt();
                GDI.OFFSETS.DATA_MEM_ADDR[j] += READER.readNextUnsignedInt();
                GDI.OFFSETS.DATA_SIZE[j] += READER.readNextUnsignedInt();
            }

            GDI.OFFSETS.BSS_MEM_ADDR += READER.readNextUnsignedInt();
            GDI.OFFSETS.BSS_SIZE += READER.readNextUnsignedInt();
            GDI.OFFSETS.BSS_ENTRY += READER.readNextUnsignedInt();
            GDI.OFFSETS.HAS_BSS += true;
        } 
        
        catch (Exception e)  
        {
            throw new IOException(this, "GDI HEADER failed to read");
        }
    }
    
    
    /* RUNS A COROUTINE CHECK TO DETERMINE THE CORRESPONDING LOAD SPECIFICATIONS */
    /* FROM THE DREAMCAST'S LANGUAGE COMPILER */

    public Collection<LoadSpec> LOAD_SUPPORTED_SPECS(ByteProvider BYTE_PROVIDER, BinaryReader READER, long READER_LEN) throws IOException
    {
        List<LoadSpec> LOAD_SPECS = new ArrayList<>();

        // ASSUMES THE BITWISE LENGTH OF READING FROM 16 BIT AND 32 BIT REGISTERS
        // RELATIVE TO A 2KB FLAG

        int[] READER_SIZE = {16 * 2048 || 32 * 2048};

        READER_LEN += READER.length();

        // PROVIDED AN ARBITARY VALUE TO REPRESENT THE READER SIZE
        // ASSUME THAT THE SIZE MATCHES, LOAD THE SPECIFIED SPECS FROM THE BINARY

        for (int SIZES : READER_SIZE)
        {
            LOAD_SPECS.add(new LoadSpec(this, 0, new LanguageCompilerSpecPair("SUPERH4:HLE:32:default", "default"), true));
        }

        return LOAD_SPECS;
    }
}
