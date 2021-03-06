package org.nd4j.compression.impl;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.compression.CompressedDataBuffer;
import org.nd4j.linalg.compression.CompressionDescriptor;
import org.nd4j.linalg.compression.CompressionType;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Dummy NoOp compressor, that actually does no compression.
 *
 * @author raver119@gmail.com
 */
public class NoOp extends AbstractCompressor {
    /**
     * This method returns compression descriptor. It should be unique for any compressor implementation
     *
     * @return
     */
    @Override
    public String getDescriptor() {
        return "NOOP";
    }

    /**
     * This method returns compression type provided by specific NDArrayCompressor implementation
     *
     * @return
     */
    @Override
    public CompressionType getCompressionType() {
        return CompressionType.LOSSLESS;
    }

    @Override
    public DataBuffer decompress(DataBuffer buffer) {

        CompressedDataBuffer comp = (CompressedDataBuffer) buffer;

        DataBuffer result = Nd4j.createBuffer(comp.length(), false);
        Nd4j.getMemoryManager().memcpy(result, buffer);

        return result;
    }

    @Override
    public DataBuffer compress(DataBuffer buffer) {

        CompressionDescriptor descriptor = new CompressionDescriptor(buffer, this);

        BytePointer ptr = new BytePointer(buffer.length() * buffer.getElementSize());
        CompressedDataBuffer result = new CompressedDataBuffer(ptr, descriptor);

        Nd4j.getMemoryManager().memcpy(result, buffer);

        return result;
    }

    @Override
    protected CompressedDataBuffer compressPointer(DataBuffer.TypeEx srcType, Pointer srcPointer, int length, int elementSize) {

        CompressionDescriptor descriptor = new CompressionDescriptor();
        descriptor.setCompressionType(getCompressionType());
        descriptor.setOriginalLength(length * elementSize);
        descriptor.setCompressionAlgorithm(getDescriptor());
        descriptor.setOriginalElementSize(elementSize);
        descriptor.setCompressedLength(length * elementSize);
        descriptor.setNumberOfElements(length);

        BytePointer ptr = new BytePointer(length * elementSize);

        // this Pointer.memcpy is used intentionally. This method operates on host memory ALWAYS
        Pointer.memcpy(ptr, srcPointer, length * elementSize);

        CompressedDataBuffer buffer = new CompressedDataBuffer(ptr, descriptor);

        return buffer;
    }
}
