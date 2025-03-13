#ifndef __PSON__BINARYSTRING_HPP__
#define __PSON__BINARYSTRING_HPP__

#include <vector>
#include <string>
#include <Poco/Dynamic/VarHolder.h>

namespace Pson {

class BinaryString : public std::string {
public:
    BinaryString() {}
    BinaryString(const std::string &str) : std::string(str) {}
    BinaryString(std::initializer_list<char> l) : std::string(l) {}
};
    
}

namespace Poco {
namespace Dynamic {

template <>
class VarHolderImpl<Pson::BinaryString>: public VarHolder
{
public:
	VarHolderImpl(Pson::BinaryString val): _val(val)
	{
	}

	~VarHolderImpl()
	{
	}

	const std::type_info& type() const
	{
		return typeid(Pson::BinaryString);
	}

	void convert(std::string& val) const
	{
		val = _val;
	}

	VarHolder* clone(Placeholder<VarHolder>* pVarHolder = 0) const
	{
		return cloneHolder(pVarHolder, _val);
	}

	const Pson::BinaryString& value() const
	{
		return _val;
	}

	bool isString() const
	{
		return true;
	}
private:
	VarHolderImpl();
	VarHolderImpl(const VarHolderImpl&);
	VarHolderImpl& operator = (const VarHolderImpl&);

    Pson::BinaryString _val;
};
}}


#endif // __PSON__BINARYSTRING_HPP__
