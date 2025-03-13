#ifndef __PSON__DECODER_HPP__
#define __PSON__DECODER_HPP__

#include <istream>
#include <vector>
#include <Poco/BinaryReader.h>
#include <Poco/Dynamic/Var.h>
#include <Pson/PsonParseHandler.hpp>

namespace Pson {
    class Decoder
    {
    public:
        Poco::Dynamic::Var decode(const std::string &data);
        Poco::Dynamic::Var decode(std::istream &stream);

        std::vector<std::string> dict;

    private:
        void decodeType(Poco::BinaryReader &breader, bool key = false);

        Pson::PsonParseHandler handler;
    };
}

#endif // __PSON__DECODER_HPP__
