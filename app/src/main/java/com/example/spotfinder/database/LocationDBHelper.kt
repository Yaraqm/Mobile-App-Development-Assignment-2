package com.example.spotfinder.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * A helper class to manage database creation and version management.
 */
class LocationDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOCATION_NAME TEXT,
                $COL_ADDRESS TEXT,
                $COL_LAT REAL,
                $COL_LNG REAL
            )
        """.trimIndent()
        db.execSQL(createTable)

        // Preload GTA landmark data
        val locations = listOf(
            arrayOf("CN Tower", "290 Bremner Blvd, Toronto, ON", 43.6426, -79.3871),
            arrayOf("Ripley's Aquarium of Canada", "288 Bremner Blvd, Toronto, ON", 43.6424, -79.3859),
            arrayOf("Rogers Centre", "1 Blue Jays Way, Toronto, ON", 43.6414, -79.3894),
            arrayOf("Scotiabank Arena", "40 Bay St, Toronto, ON", 43.6435, -79.3791),
            arrayOf("Royal Ontario Museum", "100 Queens Park, Toronto, ON", 43.6677, -79.3948),
            arrayOf("Art Gallery of Ontario", "317 Dundas St W, Toronto, ON", 43.6540, -79.3923),
            arrayOf("Toronto Zoo", "2000 Meadowvale Rd, Toronto, ON", 43.8177, -79.1859),
            arrayOf("Ontario Science Centre", "770 Don Mills Rd, North York, ON", 43.7161, -79.3385),
            arrayOf("Casa Loma", "1 Austin Terrace, Toronto, ON", 43.6780, -79.4094),
            arrayOf("Nathan Phillips Square", "100 Queen St W, Toronto, ON", 43.6525, -79.3839),
            arrayOf("Harbourfront Centre", "235 Queens Quay W, Toronto, ON", 43.6387, -79.3823),
            arrayOf("Distillery District", "55 Mill St, Toronto, ON", 43.6500, -79.3596),
            arrayOf("St. Lawrence Market", "93 Front St E, Toronto, ON", 43.6487, -79.3716),
            arrayOf("Eaton Centre", "220 Yonge St, Toronto, ON", 43.6544, -79.3807),
            arrayOf("Toronto Islands", "9 Queens Quay W, Toronto, ON", 43.6205, -79.3781),
            arrayOf("High Park", "1873 Bloor St W, Toronto, ON", 43.6465, -79.4637),
            arrayOf("Riverdale Park", "550 Broadview Ave, Toronto, ON", 43.6696, -79.3538),
            arrayOf("Allan Gardens", "160 Gerrard St E, Toronto, ON", 43.6615, -79.3744),
            arrayOf("Trinity Bellwoods Park", "790 Queen St W, Toronto, ON", 43.6476, -79.4149),
            arrayOf("Humber Bay Park", "100 Humber Bay Park Rd W, Etobicoke, ON", 43.6167, -79.4783),
            arrayOf("Scarborough Bluffs Park", "1 Brimley Rd S, Scarborough, ON", 43.7111, -79.2316),
            arrayOf("Guild Park and Gardens", "201 Guildwood Pkwy, Scarborough, ON", 43.7402, -79.1863),
            arrayOf("Bluffer's Park Beach", "1 Brimley Rd S, Toronto, ON", 43.7132, -79.2334),
            arrayOf("Kew-Balmy Beach", "2075 Queen St E, Toronto, ON", 43.6673, -79.2963),
            arrayOf("Woodbine Beach", "1675 Lake Shore Blvd E, Toronto, ON", 43.6623, -79.3076),
            arrayOf("Ashbridges Bay Park", "1561 Lake Shore Blvd E, Toronto, ON", 43.6598, -79.3125),
            arrayOf("Cherry Beach", "1 Cherry St, Toronto, ON", 43.6335, -79.3494),
            arrayOf("Tommy Thompson Park", "1 Leslie St, Toronto, ON", 43.6224, -79.3342),
            arrayOf("Toronto Music Garden", "479 Queens Quay W, Toronto, ON", 43.6364, -79.3927),
            arrayOf("Centennial Park", "256 Centennial Park Rd, Etobicoke, ON", 43.6553, -79.5903),
            arrayOf("Sunnybrook Park", "1132 Leslie St, Toronto, ON", 43.7270, -79.3632),
            arrayOf("Downsview Park", "70 Canuck Ave, North York, ON", 43.7425, -79.4786),
            arrayOf("Black Creek Pioneer Village", "1000 Murray Ross Pkwy, North York, ON", 43.7734, -79.5105),
            arrayOf("York University", "4700 Keele St, North York, ON", 43.7735, -79.5019),
            arrayOf("University of Toronto", "27 King's College Cir, Toronto, ON", 43.6629, -79.3957),
            arrayOf("Toronto Metropolitan University", "350 Victoria St, Toronto, ON", 43.6576, -79.3789),
            arrayOf("OCAD University", "100 McCaul St, Toronto, ON", 43.6539, -79.3921),
            arrayOf("George Brown College", "160 Kendal Ave, Toronto, ON", 43.6767, -79.4112),
            arrayOf("Centennial College", "941 Progress Ave, Scarborough, ON", 43.7852, -79.2267),
            arrayOf("Seneca College Newnham Campus", "1750 Finch Ave E, North York, ON", 43.7953, -79.3498),
            arrayOf("Humber College North Campus", "205 Humber College Blvd, Etobicoke, ON", 43.7305, -79.6060),
            arrayOf("Humber College Lakeshore Campus", "2 Colonel Samuel Smith Park Dr, Etobicoke, ON", 43.5953, -79.5330),
            arrayOf("Durham College", "2000 Simcoe St N, Oshawa, ON", 43.9459, -78.8965),
            arrayOf("Ontario Tech University", "2000 Simcoe St N, Oshawa, ON", 43.9459, -78.8950),
            arrayOf("Pickering Town Centre", "1355 Kingston Rd, Pickering, ON", 43.8350, -79.0859),
            arrayOf("Oshawa Centre", "419 King St W, Oshawa, ON", 43.8962, -78.8736),
            arrayOf("Scarborough Town Centre", "300 Borough Dr, Toronto, ON", 43.7764, -79.2570),
            arrayOf("Fairview Mall", "1800 Sheppard Ave E, North York, ON", 43.7787, -79.3452),
            arrayOf("Yorkdale Shopping Centre", "3401 Dufferin St, Toronto, ON", 43.7254, -79.4523),
            arrayOf("Sherway Gardens", "25 The West Mall, Etobicoke, ON", 43.6108, -79.5588),
            arrayOf("Square One Shopping Centre", "100 City Centre Dr, Mississauga, ON", 43.5934, -79.6440),
            arrayOf("Erin Mills Town Centre", "5100 Erin Mills Pkwy, Mississauga, ON", 43.5582, -79.7155),
            arrayOf("Bramalea City Centre", "25 Peel Centre Dr, Brampton, ON", 43.7164, -79.7243),
            arrayOf("Brampton City Hall", "2 Wellington St W, Brampton, ON", 43.6843, -79.7600),
            arrayOf("Mississauga Civic Centre", "300 City Centre Dr, Mississauga, ON", 43.5896, -79.6444),
            arrayOf("Living Arts Centre", "4141 Living Arts Dr, Mississauga, ON", 43.5919, -79.6427),
            arrayOf("Port Credit Lighthouse", "105 Lakeshore Rd W, Mississauga, ON", 43.5487, -79.5864),
            arrayOf("Jack Darling Memorial Park", "1180 Lakeshore Rd W, Mississauga, ON", 43.5292, -79.6213),
            arrayOf("Lakefront Promenade Park", "800 Lakefront Promenade, Mississauga, ON", 43.5530, -79.5625),
            arrayOf("Marie Curtis Park", "2 Forty Second St, Etobicoke, ON", 43.5930, -79.5427),
            arrayOf("Humber River Arch Bridge", "2 Humber River Rd, Toronto, ON", 43.6376, -79.4761),
            arrayOf("Weston Lions Park", "2125 Lawrence Ave W, York, ON", 43.7009, -79.5101),
            arrayOf("Christie Pits Park", "750 Bloor St W, Toronto, ON", 43.6645, -79.4197),
            arrayOf("Dufferin Grove Park", "875 Dufferin St, Toronto, ON", 43.6579, -79.4306),
            arrayOf("Queen's Park", "110 Wellesley St W, Toronto, ON", 43.6629, -79.3941),
            arrayOf("Yonge-Dundas Square", "1 Dundas St E, Toronto, ON", 43.6561, -79.3802),
            arrayOf("Toronto City Hall", "100 Queen St W, Toronto, ON", 43.6525, -79.3839),
            arrayOf("Toronto Public Library", "789 Yonge St, Toronto, ON", 43.6710, -79.3868),
            arrayOf("Union Station", "65 Front St W, Toronto, ON", 43.6452, -79.3806),
            arrayOf("Billy Bishop Toronto City Airport", "2 Eireann Quay, Toronto, ON", 43.6287, -79.3960),
            arrayOf("Pearson International Airport", "6301 Silver Dart Dr, Mississauga, ON", 43.6777, -79.6248),
            arrayOf("Ontario Place", "955 Lake Shore Blvd W, Toronto, ON", 43.6295, -79.4141),
            arrayOf("Exhibition Place", "100 Princes' Blvd, Toronto, ON", 43.6333, -79.4187),
            arrayOf("Fort York", "250 Fort York Blvd, Toronto, ON", 43.6370, -79.4043),
            arrayOf("Hockey Hall of Fame", "30 Yonge St, Toronto, ON", 43.6473, -79.3777),
            arrayOf("Elgin and Winter Garden Theatre", "189 Yonge St, Toronto, ON", 43.6549, -79.3793),
            arrayOf("Princess of Wales Theatre", "300 King St W, Toronto, ON", 43.6469, -79.3905),
            arrayOf("Roy Thomson Hall", "60 Simcoe St, Toronto, ON", 43.6469, -79.3854),
            arrayOf("St. Michael's Cathedral Basilica", "65 Bond St, Toronto, ON", 43.6567, -79.3776),
            arrayOf("St. James Cathedral", "106 King St E, Toronto, ON", 43.6505, -79.3740),
            arrayOf("St. Paul's Bloor Street", "227 Bloor St E, Toronto, ON", 43.6718, -79.3830),
            arrayOf("Massey Hall", "178 Victoria St, Toronto, ON", 43.6555, -79.3787),
            arrayOf("Meridian Hall", "1 Front St E, Toronto, ON", 43.6478, -79.3757),
            arrayOf("Canada's Wonderland", "1 Canada's Wonderland Dr, Vaughan, ON", 43.8430, -79.5393),
            arrayOf("Vaughan Mills", "1 Bass Pro Mills Dr, Vaughan, ON", 43.8256, -79.5390),
            arrayOf("Reptilia Zoo Vaughan", "2501 Rutherford Rd, Vaughan, ON", 43.8281, -79.5568),
            arrayOf("Kortright Centre for Conservation", "9550 Pine Valley Dr, Vaughan, ON", 43.8320, -79.6283),
            arrayOf("Markville Mall", "5000 Hwy 7, Markham, ON", 43.8667, -79.2722),
            arrayOf("Markham Museum", "9350 Hwy 48, Markham, ON", 43.8951, -79.2545),
            arrayOf("Pacific Mall", "4300 Steeles Ave E, Markham, ON", 43.8235, -79.3063),
            arrayOf("Toogood Pond Park", "58 Sciberras Rd, Markham, ON", 43.8723, -79.3154),
            arrayOf("Main Street Unionville", "197 Main St, Unionville, ON", 43.8691, -79.3095),
            arrayOf("Ajax Waterfront Park", "955 Lake Dr W, Ajax, ON", 43.8285, -79.0161),
            arrayOf("Whitby Harbour", "301 Watson St W, Whitby, ON", 43.8619, -78.9422),
            arrayOf("Oshawa Valley Botanical Gardens", "155 Arena St, Oshawa, ON", 43.9009, -78.8582),
            arrayOf("Darlington Provincial Park", "1600 Darlington Park Rd, Bowmanville, ON", 43.8685, -78.7575),
            arrayOf("Bowmanville Zoo", "340 King St E, Bowmanville, ON", 43.9136, -78.6781),
            arrayOf("Cobourg Beach", "138 Division St, Cobourg, ON", 43.9598, -78.1641),
            arrayOf("Aga Khan Museum", "77 Wynford Dr, North York, ON", 43.7250, -79.3331),
            arrayOf("Toronto Botanical Garden", "777 Lawrence Ave E, North York, ON", 43.7330, -79.3646)
        )


        for (loc in locations) {
            val values = ContentValues().apply {
                put(COL_LOCATION_NAME, loc[0] as String)
                put(COL_ADDRESS, loc[1] as String)
                put(COL_LAT, loc[2] as Double)
                put(COL_LNG, loc[3] as Double)
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    /**
     * Called when the database needs to be upgraded. This method will drop the old database and create a new one.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Inserts a new location into the database.
     */
    fun insertLocation(locationName: String, address: String, lat: Double, lng: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, locationName)
            put(COL_ADDRESS, address)
            put(COL_LAT, lat)
            put(COL_LNG, lng)
        }
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }

    /**
     * Retrieves a location from the database by its name.
     */
    fun getLocationByName(name: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE LOWER($COL_LOCATION_NAME) = LOWER(?)", arrayOf(name))
    }

    /**
     * Searches for locations in the database by name.
     */
    fun searchLocationsByName(name: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE LOWER($COL_LOCATION_NAME) LIKE LOWER(?)", arrayOf("%$name%"))
    }

    /**
     * Retrieves all locations from the database.
     */
    fun getAllLocations(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    /**
     * Updates a location in the database.
     */
    fun updateLocation(id: Int, locationName: String, address: String, lat: Double, lng: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, locationName)
            put(COL_ADDRESS, address)
            put(COL_LAT, lat)
            put(COL_LNG, lng)
        }
        val result = db.update(TABLE_NAME, values, "$COL_ID=?", arrayOf(id.toString()))
        return result > 0
    }

    /**
     * Deletes a location from the database.
     */
    fun deleteLocation(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(id.toString()))
        return result > 0
    }

    /**
     * A companion object to hold the database constants.
     */
    companion object {
        private const val DATABASE_NAME = "spotfinder.db"
        private const val DATABASE_VERSION = 5 // bumped version since schema changed
        const val TABLE_NAME = "locations"
        const val COL_ID = "id"
        const val COL_LOCATION_NAME = "location_name"
        const val COL_ADDRESS = "address"
        const val COL_LAT = "latitude"
        const val COL_LNG = "longitude"
    }
}
