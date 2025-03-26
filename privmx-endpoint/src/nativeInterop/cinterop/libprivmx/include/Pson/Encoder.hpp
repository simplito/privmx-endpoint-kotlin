#ifndef __PSON__ENCODER_HPP__
#define __PSON__ENCODER_HPP__

#include <ostream>
#include <Poco/BinaryWriter.h>
#include <Poco/Dynamic/Var.h>

namespace Pson {
    class Encoder
    {
    public:
        std::string encode(Poco::Dynamic::Var data);
        void encode(Poco::Dynamic::Var data, std::ostream &stream);
    private:
        void encodeType(Poco::Dynamic::Var data, Poco::BinaryWriter &bwriter);
    };
}

#endif // __PSON__ENCODER_HPP__