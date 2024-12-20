//
// PsonParseHandler.hpp
//
// Definition of the PsonParseHandler class based on Poco/JSON/ParseHandler extended with BinaryString.
//
// Copyright (c) 2020, Simplito Sp. z o.o.
// Copyright (c) 2012, Applied Informatics Software Engineering GmbH.
// and Contributors.
//
// SPDX-License-Identifier:	BSL-1.0


#ifndef PSON_ParseHandler_INCLUDED
#define PSON_ParseHandler_INCLUDED


#include <Poco/JSON/Handler.h>
#include <stack>
#include <Pson/BinaryString.hpp>


namespace Pson {

class PsonParseHandler : public Poco::JSON::Handler
{
public:
	PsonParseHandler(bool preserveObjectOrder = false);
		/// Creates the PsonParseHandler.
		///
		/// If preserveObjectOrder is true, the order of properties
		/// inside objects is preserved. Otherwise, items
		/// will be sorted by keys.

	virtual ~PsonParseHandler();
		/// Destroys the ParseHandler.

	virtual void reset();
		/// Resets the handler state.
		
	void startObject();
		/// Handles a '{'; a new object is started.

	void endObject();
		/// Handles a '}'; the object is closed.

	void startArray();
		/// Handles a '['; a new array is started.

	void endArray();
		/// Handles a ']'; the array is closed.

	void key(const std::string& k);
		/// A key is read

    Poco::Dynamic::Var asVar() const;
		/// Returns the result of the parser (an object or an array).

	virtual void value(int v);
		/// An integer value is read

	virtual void value(unsigned v);
		/// An unsigned value is read. This will only be triggered if the
		/// value cannot fit into a signed int.

#if defined(POCO_HAVE_INT64)
	virtual void value(Poco::Int64 v);
		/// A 64-bit integer value is read

	virtual void value(Poco::UInt64 v);
		/// An unsigned 64-bit integer value is read. This will only be
		/// triggered if the value cannot fit into a signed 64-bit integer.
#endif

	virtual void value(const std::string& s);
		/// A string value is read.

	virtual void value(const Pson::BinaryString& s);
		/// A binary string value is read.

	virtual void value(double d);
		/// A double value is read.

	virtual void value(bool b);
		/// A boolean value is read.

	virtual void null();
		/// A null value is read.

private:
	void setValue(const Poco::Dynamic::Var& value);
	typedef std::stack<Poco::Dynamic::Var> Stack;

    Stack        _stack;
	std::string  _key;
    Poco::Dynamic::Var _result;
	bool         _preserveObjectOrder;
};


//
// inlines
//
inline Poco::Dynamic::Var PsonParseHandler::asVar() const
{
	return _result;
}


inline void PsonParseHandler::value(int v)
{
	setValue(v);
}


inline void PsonParseHandler::value(unsigned v)
{
	setValue(v);
}


#if defined(POCO_HAVE_INT64)
inline void PsonParseHandler::value(Poco::Int64 v)
{
	setValue(v);
}


inline void PsonParseHandler::value(Poco::UInt64 v)
{
	setValue(v);
}
#endif


inline void PsonParseHandler::value(const std::string& s)
{
	setValue(s);
}


inline void PsonParseHandler::value(const Pson::BinaryString& s)
{
	setValue(s);
}


inline void PsonParseHandler::value(double d)
{
	setValue(d);
}


inline void PsonParseHandler::value(bool b)
{
	setValue(b);
}


inline void PsonParseHandler::null()
{
	Poco::Dynamic::Var empty;
	setValue(empty);
}


} // namespace Poco::JSON


#endif // PSON_ParseHandler_INCLUDED
